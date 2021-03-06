package com.jianxin.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianxin.config.GetHttpSessionConfigurator;
import com.jianxin.constant.ChatChatConstant;
import com.jianxin.entity.UserPO;
import com.jianxin.entity.UserPOExample;
import com.jianxin.entity.UserVO;
import com.jianxin.entity.ws.Message;
import com.jianxin.entity.ws.ResultMessage;
import com.jianxin.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/21
 * Time: 16:26
 * To change this template use File | Settings | File Templates.
 **/
@ServerEndpoint(value = "/user/chat", configurator = GetHttpSessionConfigurator.class)
@Component
public class JianXinEndPoint {

    private Logger logger = LoggerFactory.getLogger(JianXinEndPoint.class);

    // 通过该对象发送给指定用户
    private Session session;

    // 用于获取当前用户名
    private HttpSession httpSession;

    // 主要用于排序，让好友列表有一个先后顺序
    private static List<ChatEndpointUserMapper> onlineChatEndpointUserMapperList = Collections.synchronizedList(new ArrayList<>());

    //  为了方便查找用户,存储的数据与上面的list一样
    private static Map<String, ChatEndpointUserMapper> onlineChatEndpointUserMapperMap = new ConcurrentHashMap<>();


    /**
     * 当建立连接时调用
     *
     * @param session
     * @param config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {

        this.session = session;
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.httpSession = httpSession;

        UserPO userPO = (UserPO) httpSession.getAttribute(ChatChatConstant.ATTR_LOGIN_USER);
        logger.info(userPO.toString());


        // 将当前用户放到onlineUserMap中
        ChatEndpointUserMapper mapper = new ChatEndpointUserMapper(this, userVO);
        onlineChatEndpointUserMapperList.add(mapper);
        onlineChatEndpointUserMapperMap.put(userPO.getLoginAcct(), mapper);

        // 将该用户的用户名推送给所有的客户端
        // 获取消息，包含所有在线用户的列表
        String allOnlineUserMessage = MessageUtils.getMessage(true, false, ResultMessage.TYPE_ONLINE_MESSAGE, null, getUserVOListFromMapper());
        logger.info("allOnlineUserMessage:" + allOnlineUserMessage);

        // 仅包含当前用户信息的消息
        ArrayList<UserVO> currentUser = new ArrayList<>();
        currentUser.add(userPO);
        String currentUserMessage = MessageUtils.getMessage(true, false, ResultMessage.TYPE_ONLINE_MESSAGE, null, currentUser);
        logger.info("currentUserMessage:" + currentUserMessage);

        // 调用广播方法
        broadcastAllUsers(allOnlineUserMessage, currentUserMessage);


    }



    /**
     * 获取onlineChatEndpointUserMapperList中的用户列表
     *
     * @return
     */
    private List<UserVO> getUserVOListFromMapper() {

        List<UserVO> userVOList = new ArrayList<>();
        for (ChatEndpointUserMapper chatEndpointUserMapper : onlineChatEndpointUserMapperList) {
            UserVO userVO = chatEndpointUserMapper.getUserVO();
            userVOList.add(userVO);
        }
        return userVOList;
    }


    /***
     * 将message广播给所有用户,进行更新好友列表
     * 如果不是该用户的其他用户只传送该用户的message
     * 如果是该用户那么将全部用户都更新到好友列表中
     * @param allOnlineUserMessage
     * @param currentUserMessage
     */
    private void broadcastAllUsers(String allOnlineUserMessage, String currentUserMessage) {

        try {

            UserVO currentUser = getCurrentUser();
            String currentUserLoginAcct = currentUser.getLoginAcct();


            for (ChatEndpointUserMapper chatEndpointUserMapper : onlineChatEndpointUserMapperList) {
                String loginAcct = chatEndpointUserMapper.getUserVO().getLoginAcct();
                // 如果是当前用户
                JianXinEndPoint chatEndpoint = chatEndpointUserMapper.getChatEndpoint();
                if (currentUserLoginAcct.equals(loginAcct)) {
                    // 那么更新整个好友列表
                    chatEndpoint.session.getBasicRemote().sendText(allOnlineUserMessage);
                } else {
                    // 如果不是当前用户
                    // 仅将当前用户更新到好友列表
                    chatEndpoint.session.getBasicRemote().sendText(currentUserMessage);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 当接收到消息时被调用
     *
     * @param message 通过message中的toName，确定接收者
     * @param session 发送者的session
     */
    @OnMessage
    public void onMessage(String message, Session session) {


        try {

            logger.info("接收到客户端发来的message:" + message);

            ObjectMapper objectMapper = new ObjectMapper();
            Message msg = objectMapper.readValue(message, Message.class);
            // 获取就接收者
            String toName = msg.getToName();

            // 获取消息数据
            String data = msg.getMessage();

            // 获取发送者
            UserVO currentUser = getCurrentUser();



            // 获取服务端发送给客户端的消息格式
            String sendMessage = MessageUtils.getMessage(false, msg.isPicture(), null, currentUser, data);

            // 获取接受者的服务端
            ChatEndpointUserMapper chatEndpointUserMapper = onlineChatEndpointUserMapperMap.get(toName);
            JianXinEndPoint chatEndpoint = chatEndpointUserMapper.getChatEndpoint();

            // 发送
            chatEndpoint.session.getBasicRemote().sendText(sendMessage);

            logger.info("发送到客户端的message:" + sendMessage);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 关闭连接时,关闭浏览器会调用该方法
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {


        UserVO currentUser = getCurrentUser();
        String currentLoginAcct = currentUser.getLoginAcct();

        // 将其从mapper中删除
        ChatEndpointUserMapper currentMapper = onlineChatEndpointUserMapperMap.get(currentLoginAcct);
        onlineChatEndpointUserMapperMap.remove(currentLoginAcct);

        // 将其从list中删除
        onlineChatEndpointUserMapperList.remove(currentMapper);



        logger.info("用户： "+currentLoginAcct+" 登出成功");


        // 封装离线消息

        String message = MessageUtils.getMessage(true, false, ResultMessage.TYPE_OFFLINE_MESSAGE, currentUser, currentUser);

        // 广播给所有用户
        offlineBroadcastAll(message);

        logger.info("用户："+currentLoginAcct+" 断开连接");




    }

    /**
     * 该离线的用户信息发送给其他的用户，在好友列表中删除
     *
     * @param message
     */
    private void offlineBroadcastAll(String message) {


        try {
            for (ChatEndpointUserMapper chatEndpointUserMapper : onlineChatEndpointUserMapperList) {
                String loginAcct = chatEndpointUserMapper.getUserVO().getLoginAcct();
                ChatEndpointUserMapper chatEndpointUserMapper1 = onlineChatEndpointUserMapperMap.get(loginAcct);
                JianXinEndPoint chatEndpoint = chatEndpointUserMapper.getChatEndpoint();

                chatEndpoint.session.getBasicRemote().sendText(message);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}





}
