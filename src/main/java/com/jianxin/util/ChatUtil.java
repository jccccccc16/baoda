package com.jianxin.util;


import com.jianxin.constant.ChatChatConstant;
import com.jianxin.entity.UserPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/10
 * Time: 9:30
 * To change this template use File | Settings | File Templates.
 **/
public class ChatUtil {

    private static Logger logger = LoggerFactory.getLogger(ChatUtil.class);

    public static String md5(String source){

        if(source == null || source.equals("")){
            throw new RuntimeException(ChatChatConstant.MESSAGE_STRING_INVALIDATE);
        }


        try {
            String algorithm = "md5";
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);

            byte[] input = source.getBytes();
            byte[] output = messageDigest.digest(input);

            int signum = 1;
            BigInteger bigInteger = new BigInteger(signum, output);

            // 按照16进制转换为字符串
            int radix = 16;
            String encoded = bigInteger.toString(radix).toUpperCase();

            return encoded;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.warn(e.getMessage()+"  加密出错");
        }

        return null;
    }

    /**
     * 清洗数据，洗去头尾空格
     * @param userPO
     * @return
     */
    public static UserPO userPODataClean(UserPO userPO){

        String loginAcctTrim = userPO.getLoginAcct().trim();
        userPO.setLoginAcct(loginAcctTrim);

        String userPswdTrim = userPO.getUserPswd().trim();
        userPO.setUserPswd(userPswdTrim);

        String nameTrim = userPO.getName().trim();
        userPO.setName(nameTrim);

        String usernameTrim = userPO.getUsername().trim();
        userPO.setUsername(usernameTrim);

        return userPO;

    }

    /**
     * 最终效果 http://localhost:8080+/login.html 方便部署后的重定向
     * @param request
     * @return
     */
    public static String getPath(HttpServletRequest request,String url){

        String path="";

        // 最终效果 http://localhost:8080+/login.html
        String fullUrl="";

        // 获取协议
        String scheme = request.getScheme();

        // 获取服务器名字
        String serverName = request.getServerName();

        // 获取端口号
        int serverPort = request.getServerPort();

        // 获取应用名
        String contextPath = request.getContextPath();

        if(contextPath==null || contextPath.equals("")){
            // http://localhost:8080/应用名
            path = scheme+"://"+serverName+":"+serverPort;
        }else{
            path = scheme+"://"+serverName+":"+serverPort+"/"+contextPath;
        }

        fullUrl = path+url;

        return fullUrl;


    }




}
