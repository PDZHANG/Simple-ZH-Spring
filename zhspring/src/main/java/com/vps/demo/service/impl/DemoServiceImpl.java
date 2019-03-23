package com.vps.demo.service.impl;

import com.vps.demo.service.DemoService;
import com.vps.webmvc.annotaion.ZHService;

@ZHService
public class DemoServiceImpl implements DemoService {


    @Override
    public String get(String name) {
        return "Geted >>>>>>" + name + "<<<<<<<<";
    }
}
