package com.wojiushiwo.controller;

import com.wojiushiwo.annotation.AutoWired;
import com.wojiushiwo.annotation.Controller;
import com.wojiushiwo.annotation.RequestMapping;
import com.wojiushiwo.service.UserService;

/**
 * Created by meng on 2018/7/8.
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @AutoWired
    private UserService userService;
    @RequestMapping("/sayHello")
    public void sayHello(String name){
        userService.sayHello(name);
    }
}
