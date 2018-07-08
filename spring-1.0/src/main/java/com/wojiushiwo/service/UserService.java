package com.wojiushiwo.service;

import com.wojiushiwo.annotation.Service;

/**
 * Created by meng on 2018/7/8.
 */
@Service
public class UserService {
    public void sayHello(String name){
        System.out.println("Hello "+name);
    }
}
