package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.client.CosFeignClient;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosServiceImpl implements CosService {

    @Autowired
    private CosFeignClient cosFeignClient;
    @Override
    public CosUploadVo upload(MultipartFile multipartFile, String path) {
        //1. 远程调用
        Result<CosUploadVo> upload = cosFeignClient.upload(multipartFile, path);
        Integer status = upload.getCode();
        if(status != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        CosUploadVo data = upload.getData();
        if(data == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        //2.返回
        return data;
    }
}
