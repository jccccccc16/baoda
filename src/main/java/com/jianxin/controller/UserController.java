package com.jianxin.controller;

import com.jianxin.constant.ChatChatConstant;
import com.jianxin.entity.UserLoginVO;
import com.jianxin.entity.UserPO;
import com.jianxin.entity.UserRegisterVO;
import com.jianxin.service.api.UserService;
import com.jianxin.util.ChatUtil;
import com.jianxin.util.ResultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/21
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 **/
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 注册
     * @param userRegisterVO
     * @return
     */
    @RequestMapping("/user/register.json")
    @ResponseBody
    public ResultEntity<String> register(@RequestBody UserRegisterVO userRegisterVO){

        // userRegisterVO
        if(userRegisterVO==null){
            return ResultEntity.failed(ChatChatConstant.MESSAGE_REGISTER_DATA_NULL);
        }

        // 判断两次输入的密码是否一致
        if(!userRegisterVO.getUserPswd().equals(userRegisterVO.getRepeatUserPswd())){

            // 如果不一致
            return ResultEntity.failed(ChatChatConstant.MESSAGE_PASSWORD_NOT_SAME);
        }

        // 如果没有错误信息，保存用户信息
        // 将密码进行加密处理
        String passwordEncoded = ChatUtil.md5(userRegisterVO.getUserPswd());
        userRegisterVO.setUserPswd(passwordEncoded);

        UserPO userPO = new UserPO();
        BeanUtils.copyProperties(userRegisterVO,userPO);

        userPO.setIsLogin(0);

        try{

            userService.saveUser(userPO);
            return ResultEntity.successWithoutData();
        }catch (Exception e){
            e.printStackTrace();
            logger.warn("注册失败");
            return ResultEntity.failed(ChatChatConstant.MESSAGE_SAVE_USER_FAILED);
        }
    }


    @RequestMapping("/user/login.json")
    @ResponseBody
    public ResultEntity<String> login(@RequestBody UserLoginVO userLoginVO,
                                      HttpSession session){

        String loginAcct = userLoginVO.getLoginAcct();
        String userPswd = userLoginVO.getUserPswd();

        // 根据loginAcct查找用户
        UserPO userByLoginAcct = userService.getUserByLoginAcct(loginAcct);

        // 没有该用户
        if(userByLoginAcct==null){
            return ResultEntity.failed(ChatChatConstant.MESSAGE_LOGIN_FAILED);
        }

        // 密码校验
        // 对明文进行加密
        String userPswdEncoded = ChatUtil.md5(userPswd);

        String userPswdOrigin = userByLoginAcct.getUserPswd();

        // 如果不匹配
        if(!userPswdOrigin.equals(userPswdEncoded)){

            return ResultEntity.failed(ChatChatConstant.MESSAGE_LOGIN_FAILED);
        }

        logger.info("登录成功");

        session.setAttribute(ChatChatConstant.ATTR_LOGIN_USER,userByLoginAcct);

        return ResultEntity.successWithoutData();

    }




}
