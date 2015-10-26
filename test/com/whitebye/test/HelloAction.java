package com.whitebye.test;

import com.whitebye.redbye.annotation.*;
import com.whitebye.redbye.annotation.RequestAction;
import com.whitebye.redbye.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/20 0020.
 */
@RequestAction(path = "/hello")
public class HelloAction {

    @HandleMethod(method = RequestMethod.GET, path = "/world")
    @Return(type = ReturnType.STRING)
    public String world(){
        System.out.println("hello world");
        return "hello world by redbye";
    }

    @HandleMethod(path = "/registerUser")
    @Forward(url = "/WEB-INF/jsp/showUser.jsp")
    @Return(type = ReturnType.OBJECT, name = "showUser")
    public String registerUser(String username, int age){
        String showUser = "用户名：" + username + ",年龄：" + age;
        System.out.println("用户开始注册...，注册成功，" + showUser);
        return showUser;
    }

    @HandleMethod(path = "/registerUser2")
    @Forward(url = "/WEB-INF/jsp/showUser2.jsp")
    @Return(type = ReturnType.OBJECT, name = "user")
    public User registerUser2(User user){
        String showUser = "用户名：" + user.getUsername();
        System.out.println("用户开始注册...，注册成功，" + showUser);
        return user;
    }

    @HandleMethod(path = "/queryUser")
    @Forward(url = "/WEB-INF/jsp/queryUser.jsp")
    @Return(type = ReturnType.MAP)
    public Map queryUser(String username, int age){
        String showUser = "用户名：" + username + ",年龄：" + age;
        System.out.println("开始查询用户，查询到，" + showUser);
        Map map = new HashMap();
        map.put("username", username);
        map.put("age", age);
        return map;
    }

    @HandleMethod(path = "/registerUser3")
    @Forward(url = "/WEB-INF/jsp/showUser2.jsp")
    @Return(type = ReturnType.OBJECT, name = "user")
    public User registerUser3(User user, String extra, int extra2){
        String showUser = "用户名：" + user.getUsername();
        System.out.println("用户开始注册...，注册成功，" + showUser);
        System.out.println("额外属性：，" + extra);
        System.out.println("额外属性2：，" + extra2);
        return user;
    }

    @HandleMethod(path = "/registerUser4")
    @Forward(url = "/WEB-INF/jsp/showUser2.jsp")
    @Return(type = ReturnType.OBJECT, name = "user")
    public User registerUser4(User user, String extra, User user2){
        System.out.println("user=" + user.getUsername());
        System.out.println("extra=" + extra);
        System.out.println("user2=" + user2.getUsername());
        return user;
    }

}
