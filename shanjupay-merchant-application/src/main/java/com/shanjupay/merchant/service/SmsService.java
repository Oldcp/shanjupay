package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shanjupay.common.domain.BusinessException;

import java.nio.BufferOverflowException;

/**
 * @author old money
 * @create 2022-03-11 15:59
 */
public interface SmsService {

    /**
     * 向验证码服务请求发送验证码
     * @param phone 手机号
     * @return 验证码对应的key
     */
    String sendMsg(String phone);




    /**
     *校验验证码 ，抛出异常则校验无效
     * @param verifiyKey 验证码对应的key
     * @param verifiyCode 验证码
     *
     */
    void checkVerifiyCode(String verifiyKey,String verifiyCode) throws BusinessException;

}
