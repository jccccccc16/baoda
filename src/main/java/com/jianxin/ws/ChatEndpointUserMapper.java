package com.jianxin.ws;


import com.jianxin.entity.UserVO;

/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/11
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 **/
public class ChatEndpointUserMapper {

    private JianXinEndPoint chatEndpoint;
    private UserVO userVO;
    private boolean isCurrentLogin;

    public ChatEndpointUserMapper(JianXinEndPoint chatEndpoint, UserVO userVO) {
        this.chatEndpoint = chatEndpoint;
        this.userVO = userVO;
    }

    public ChatEndpointUserMapper() {
    }



    public JianXinEndPoint getChatEndpoint() {
        return chatEndpoint;
    }

    public void setChatEndpoint(JianXinEndPoint chatEndpoint) {
        this.chatEndpoint = chatEndpoint;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }




    @Override
    public boolean equals(Object obj) {

        ChatEndpointUserMapper otherMapper = (ChatEndpointUserMapper) (obj);
        String thisLoginAcct = this.getUserVO().getLoginAcct();
        String otherLoginAcct = otherMapper.getUserVO().getLoginAcct();
        if(thisLoginAcct.equals(otherLoginAcct)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        return "ChatEndpointUserMapper{" +
                "chatEndpoint=" + chatEndpoint +
                ", userVO=" + userVO +
                '}';
    }
}
