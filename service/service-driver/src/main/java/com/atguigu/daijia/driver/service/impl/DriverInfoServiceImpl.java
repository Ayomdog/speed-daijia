package com.atguigu.daijia.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.driver.mapper.DriverAccountMapper;
import com.atguigu.daijia.driver.mapper.DriverInfoMapper;
import com.atguigu.daijia.driver.mapper.DriverLoginLogMapper;
import com.atguigu.daijia.driver.mapper.DriverSetMapper;
import com.atguigu.daijia.driver.service.DriverInfoService;
import com.atguigu.daijia.model.entity.driver.DriverAccount;
import com.atguigu.daijia.model.entity.driver.DriverInfo;
import com.atguigu.daijia.model.entity.driver.DriverLoginLog;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {


    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private DriverInfoMapper driverInfoMapper;
    @Autowired
    private DriverAccountMapper driverAccountMapper;
    @Autowired
    private DriverSetMapper driverSetMapper;
    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;
    /**
     * 小程序登录
     * @param code
     * @return
     */
    @Override
    public Long login(String code) {
        //1.根据code appid 秘钥获取openid
        String openid = null;
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openid = sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        //2. 根据openid查询司机信息
        DriverInfo driverInfo = driverInfoMapper.selectById(openid);
        //3. 如果司机是第一次登录
        if(driverInfo == null){
            //3.1 添加司机基本信息
            driverInfo = new DriverInfo();
            driverInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            driverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            driverInfo.setWxOpenId(openid);
            driverInfoMapper.insert(driverInfo);
            //3.2 初始化司机账户
            DriverAccount driverAccount = new DriverAccount();
            driverAccount.setDriverId(driverInfo.getId());
            driverAccountMapper.insert(driverAccount);
            //3.3 初始化司机设置
            DriverSet driverSet = new DriverSet();
            driverSet.setDriverId(driverInfo.getId());
            driverSet.setOrderDistance(new BigDecimal(0)); // 0 无限制
            driverSet.setAcceptDistance(new BigDecimal(SystemConstant.ACCEPT_DISTANCE)); // 默认接单范围 5公里
            driverSet.setIsAutoAccept(0); // 0: 否 1: 是 是否自动接单
            driverSetMapper.insert(driverSet);
        }
        //4.记录司机登录信息
        DriverLoginLog driverLoginLog = new DriverLoginLog();
        driverLoginLog.setDriverId(driverInfo.getId());
        driverLoginLog.setMsg("小程序登录");
        driverLoginLogMapper.insert(driverLoginLog);
        //5. 返回司机id
        return driverInfo.getId();
    }

    /**
     * 获取司机登录信息
     * @param driverId
     * @return
     */
    @Override
    public DriverLoginVo getDriverLoginInfo(String driverId) {
        //1.通过driverId 获取司机信息
        DriverInfo driverInfo = driverInfoMapper.selectById(driverId);
        //2. 创建DriverLoginVO 对象
        DriverLoginVo driverLoginVo = new DriverLoginVo();
        //3. 拷贝司机信息到DriverLoginVO
        BeanUtils.copyProperties(driverInfo, driverLoginVo);
        //4.判断司机是否有人脸id模型
        String faceModelId = driverInfo.getFaceModelId();
        if(!StringUtils.isBlank(faceModelId)){
            //4.1 有则将 isArchiveFace 设置为true
            driverLoginVo.setIsArchiveFace(true);
        }
        //5. 返回
        return driverLoginVo;
    }
}