package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.login.AyomLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverController {

    @Autowired
    private DriverService driverService;

    @Operation(summary = "小程序登录")
    @GetMapping("/login/{code}")
    public Result<String> login(@PathVariable String code){
        return Result.ok(driverService.login(code));
    }

    @Operation(summary = "获取司机登录信息")
    //@AyomLogin
    @GetMapping("/getDriverLoginInfo")
    public Result<DriverLoginVo> getDriverLoginInfo() {
        DriverLoginVo driverLoginVo =  driverService.getDriverLoginInfo();
        return Result.ok(driverLoginVo);
    }
}

