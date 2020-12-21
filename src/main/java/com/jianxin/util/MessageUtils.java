package com.jianxin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianxin.entity.UserVO;
import com.jianxin.entity.ws.ResultMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/11
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 **/
public class MessageUtils {

    /**
     * 返回json字符串
     * @param isSystemMessage
     * @param message
     * @return
     */
    public static String getMessage(boolean isSystemMessage,
                                    boolean isPicture,
                                    String type,
                                    UserVO fromUser,
                                    Object message){
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setIsSystem(isSystemMessage);
        resultMessage.setType(type);
        resultMessage.setMessage(message);
        resultMessage.setIsPicture(isPicture);
        if(fromUser!=null){
            resultMessage.setFromUser(fromUser);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(resultMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
