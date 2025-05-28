package com.atguigu.daijia.customer.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.client.CustomerInfoFeignClient;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {

    //注入远程调用接口
    @Autowired
    private CustomerInfoFeignClient client;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public String login(String code) {
        //1.用code 进行远程调用
        Result<Long> result = client.login(code);
        //2.如果返回失败了，返回错误提示
        Integer status = result.getCode();
        if(status != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //3.拿到远程调用结果获取用户id
        Long customerId = result.getData();
        //4. 如果用户id为空 返回错误提示
        if(customerId == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //5.生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        //6.将用户id跟token 存入redis，设置过期时间
        // key: toeken  value: 用户id
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                                            customerId.toString(),
                                            RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                                            TimeUnit.SECONDS);
        //7.返回token
        return token;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(String token) {
        //1.通过token从redis中获取用户id
        String customerId = (String) redisTemplate.opsForValue()
                                .get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        //2. 如果用户id为空，抛出错误信息
        if(StringUtils.isBlank(customerId)){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //3. 根据用户id进行远程调用
        Result<CustomerLoginVo> customerLoginInfo = client.getCustomerLoginInfo(Long.parseLong(customerId));
        // 3.1 如果调用失败返回错误信息
        if(customerLoginInfo.getCode() != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        CustomerLoginVo loginVo = customerLoginInfo.getData();
        if(loginVo == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //4. 返回用户信息
        return loginVo;
    }

    @Override
    public CustomerLoginVo getCustomerInfo(Long customerId) {

        //3. 根据用户id进行远程调用
        Result<CustomerLoginVo> customerLoginInfo = client.getCustomerLoginInfo(customerId);
        // 3.1 如果调用失败返回错误信息
        if(customerLoginInfo.getCode() != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        CustomerLoginVo loginVo = customerLoginInfo.getData();
        if(loginVo == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //4. 返回用户信息
        return loginVo;

    }

    //更新用户微信手机号
    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        Result<Boolean> booleanResult = client.updateWxPhoneNumber(updateWxPhoneForm);
        return true;
    }
}
