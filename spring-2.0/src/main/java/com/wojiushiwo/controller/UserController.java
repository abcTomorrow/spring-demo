package com.wojiushiwo.controller;

import com.wojiushiwo.annotation.AutoWired;
import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.RequestMapping;
import com.wojiushiwo.annotation.RequestParam;
import com.wojiushiwo.service.UserService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by meng on 2018/7/8.
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @AutoWired
    private UserService userService=new UserService();

    @RequestMapping("/sayHello")
    public void sayHello(HttpServletRequest request, @RequestParam String name) {
        userService.sayHello(name);
    }
}
