package com.study.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/hello")
public class HelloController {

    @RequestMapping("/hello")
    public String getHello() {
        System.out.println("这里是Hello");
        return "hello world";
    }

    @RequestMapping("/test1")
    public String getTest1() {
        System.out.println("这里是Test1");
        return "test1 content";
    }

    @RequestMapping("/test2")
    public String getTest2() {
        System.out.println("这里是Test2");
        return "test2 content";
    }

}
