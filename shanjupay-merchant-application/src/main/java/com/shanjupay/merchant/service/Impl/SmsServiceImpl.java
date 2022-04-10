package com.shanjupay.merchant.service.Impl;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author old money
 * @create 2022-03-11 16:03
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    /**
     * 从nacos配置文件中获取短信验证码服务的url
     */
    @Value("${sms.url}")
    String url;


    /**
     * 从nacos配置文件中获取短信验证码的有效期
     */
    @Value("${sms.effectiveTime}")
    String effectiveTime;


    @Autowired
    RestTemplate restTemplate;


    /**
     * 向验证码服务请求发送验证码
     * @param phone 手机号
     * @return
     */
    @Override
    public String sendMsg(String phone) {
        //向验证码服务发送请求获取验证码
        String sms_url = url + "generate?name=sms&effectiveTime=" + effectiveTime ;

        //请求体
        Map<String,Object> body = new HashMap<>();
        body.put("mobile",phone);
        //请求头
        HttpHeaders httpHeaders =new HttpHeaders();
        //指定Content-Type: application/json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //请求信息,传入body，header
        HttpEntity httpEntity = new HttpEntity(body,httpHeaders);

        //向url请求
        ResponseEntity<Map> exchange = null;
        Map bodyMap = null;
        try {
            exchange = restTemplate.exchange(sms_url, HttpMethod.POST, httpEntity, Map.class);

            log.info("请求验证码服务，得到响应:{}", JSON.toJSONString(exchange));
            bodyMap = exchange.getBody();

        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("发送验证码失败");
        }
        if (bodyMap == null || bodyMap.get("result") == null){
            throw new RuntimeException("发送验证码失败");
        }

        Map result = (Map) bodyMap.get("result");
        String key = (String) result.get("key");
        return key;
    }




    /**
     * 校验验证码
     * @param verifiyKey 验证码对应的key
     * @param verifiyCode 验证码
     */
    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) throws BusinessException{
        //校验验证码的url
        String url = "http://localhost:56085/sailing/verify?name=sms&verificationCode="+verifiyCode+"&verificationKey="+verifiyKey;

        Map bodyMap = null;
        try {
            //使用restTemplate请求验证码服务
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            log.info("请求验证码服务，得到响应:{}", JSON.toJSONString(exchange));
            bodyMap = exchange.getBody();
        }catch (Exception e){
            e.printStackTrace();
//            throw new RuntimeException("校验验证码失败");
            throw new BusinessException(CommonErrorCode.E_100102);
        }
        if(bodyMap == null || bodyMap.get("result") == null || !(Boolean) bodyMap.get("result")){
//            throw new RuntimeException("校验验证码失败");
            throw new BusinessException(CommonErrorCode.E_100102);
        }
    }
}
