package com.atguigu.daijia.common.login;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.common.util.AuthContextHolder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
//切面类
public class AyomLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    //环绕通知
    //切入点表达式，指定对那些规则的方法进行增强
    @Around("execution(* com.atguigu.daijia.*.controller.*.*(..)) && @annotation(ayomLogin)")
    public Object login(ProceedingJoinPoint proceedingJoinPoint, AyomLogin ayomLogin) throws Throwable {
        //1.获取requst对象
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = sra.getRequest();
        //2.从请求头拿取token
        String token = request.getHeader("token");
        //3. 如果token为空，则抛出异常
        if(StringUtils.isBlank(token)){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        //3.通过token从redis中获取用户id
        String customerId = (String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        //4.如果用户id为空，则抛出异常
        if(StringUtils.isBlank(customerId)){
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        //5.将用户id存入ThreadLocal中，供后续使用
        AuthContextHolder.setUserId(Long.parseLong(customerId));
        return proceedingJoinPoint.proceed();
    }

}
