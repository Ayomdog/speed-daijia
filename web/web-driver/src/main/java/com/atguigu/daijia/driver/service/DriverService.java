package com.atguigu.daijia.driver.service;

import com.atguigu.daijia.model.vo.driver.DriverLoginVo;

public interface DriverService {


    //小程序登录
    String login(String driverId);

    //获取司机登录信息
    DriverLoginVo getDriverLoginInfo();
}
