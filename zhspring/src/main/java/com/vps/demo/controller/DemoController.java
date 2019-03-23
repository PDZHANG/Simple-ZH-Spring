package com.vps.demo.controller;

import com.vps.demo.service.DemoService;
import com.vps.webmvc.annotaion.ZHAutowired;
import com.vps.webmvc.annotaion.ZHController;
import com.vps.webmvc.annotaion.ZHRequestMapping;
import com.vps.webmvc.annotaion.ZHRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ZHController
@ZHRequestMapping("/demo")
public class DemoController {


    @ZHAutowired
    private DemoService demoService;


    @ZHRequestMapping("/query")
    public String query(HttpServletRequest request , HttpServletResponse response,@ZHRequestParam("name") String name){
        String result = demoService.get(name);

        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result;
    }

    @ZHRequestMapping("/add")
    public void add(HttpServletRequest request , HttpServletResponse response,@ZHRequestParam("name") String name , @ZHRequestParam("age") Integer age){
        try {
            response.getWriter().write("add name:" + name + "   age:" + age);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @ZHRequestMapping("/remove")
    public void remove(HttpServletRequest request , HttpServletResponse response,@ZHRequestParam("name") String name ){


    }

}
