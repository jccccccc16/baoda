package com.jianxin.service.api;

import com.jianxin.entity.UserPO;

/**
 * Created by IntelliJ IDEA.
 * User: cjc
 * Date: 2020/12/9
 * Time: 21:43
 * To change this template use File | Settings | File Templates.
 **/
public interface UserService {

    public void saveUser(UserPO userPO);

    public UserPO getUserByLoginAcct(String loginAcct);

}
