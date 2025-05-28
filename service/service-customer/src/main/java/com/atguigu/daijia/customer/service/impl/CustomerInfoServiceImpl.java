package com.atguigu.daijia.customer.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.config.WxConfigOperator;
import com.atguigu.daijia.customer.config.WxConfigProperties;
import com.atguigu.daijia.customer.mapper.CustomerInfoMapper;
import com.atguigu.daijia.customer.mapper.CustomerLoginLogMapper;
import com.atguigu.daijia.customer.service.CustomerInfoService;
import com.atguigu.daijia.model.entity.customer.CustomerInfo;
import com.atguigu.daijia.model.entity.customer.CustomerLoginLog;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private CustomerInfoMapper customerInfoMapper;
    @Autowired
    private CustomerLoginLogMapper customerLoginLogMapper;
    @Override
    public Long login(String code) {
        //1.拿到code值，使用工具包对象获取openid
        String openid = null;
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openid = sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        //2.拿到openid值，查询数据库中是否存在该openid对应的客户信息
        LambdaQueryWrapper<CustomerInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerInfo::getWxOpenId, openid);
        CustomerInfo customerInfo = customerInfoMapper.selectOne(queryWrapper);

        if(customerInfo == null){
            //3.如果不存在则是第一次登录，插入用户信息
            customerInfo = new CustomerInfo();
            customerInfo.setWxOpenId(openid);
            customerInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            customerInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            customerInfoMapper.insert(customerInfo);
        }
        //4.记录登录日志信息
        CustomerLoginLog customerLoginLog = new CustomerLoginLog();
        customerLoginLog.setCustomerId(customerInfo.getId());
        customerLoginLog.setMsg("小程序登录");
        customerLoginLogMapper.insert(customerLoginLog);
        //5.存在，直接返回用户id
        return customerInfo.getId();
    }

    /**
     * 获取登录用户信息
     * @param customerId
     * @return
     */
    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        //1. 根据id查询到用户信息
        CustomerInfo customerInfo = customerInfoMapper.selectById(customerId);
        // 2. 将用户信息封装到CutomerLoginVo对象
        CustomerLoginVo customerLoginVo = new CustomerLoginVo();
        BeanUtils.copyProperties(customerInfo,customerLoginVo);
        // 3. 判断是否绑定手机号
        String phone = customerInfo.getPhone();
        boolean isBindPhone = StringUtils.hasText(phone);
        customerLoginVo.setIsBindPhone(isBindPhone);
        // 4. 返回CustomerLoginVo对象
        return customerLoginVo;
    }

    ////更新客户微信手机号码
    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        //1 根据code值获取微信绑定手机号码
        try {
            WxMaPhoneNumberInfo phoneNoInfo =
                    wxMaService.getUserService().getPhoneNoInfo(updateWxPhoneForm.getCode());
            String phoneNumber = phoneNoInfo.getPhoneNumber();

            //更新用户信息
            Long customerId = updateWxPhoneForm.getCustomerId();
            CustomerInfo customerInfo = customerInfoMapper.selectById(customerId);
            customerInfo.setPhone(phoneNumber);
            customerInfoMapper.updateById(customerInfo);

            return true;
        } catch (WxErrorException e) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }
}
