package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverInfoFeignClient client;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 小程序登录
     * @param code
     * @return
     */
    @Override
    public String login(String code) {
        // 1.调用远程方法,传入code,获取用户id
        Result<Long> result = client.login(code);
        Integer status = result.getCode();
        //2.如果返回状态码不为200，抛出异常
        if(status != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //3.获取用户id
        Long driverId = result.getData();
        //4. 如果返回数据为空，抛出异常
        if(driverId == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //5.生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        //6.将token存入redis,设置过期时间
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                                            driverId.toString(),
                                            RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                                            TimeUnit.SECONDS);
        //7.返回token
        return token;
    }
}
