package com.shanjupay.merchant;

import com.sun.javafx.collections.MappingChange;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author old money
 * @create 2022-03-11 15:30
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RestTemplateTest {

    @Autowired
    private RestTemplate restTemplate;


    //测试使用RestTemplate作为http的客户端向http服务端发送请求
    @Test
    public void gethtml(){
        String url = "http://www.baidu.com/";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        System.out.println(body);
    }


    //向验证码服务端发送请求，获取验证码
    //http://localhos:56085/sailing/generate?effectiveTime=600&name=sms
    @Test
    public void getSmsCode(){

        String url = "http://localhos:56085/sailing/generate?effectiveTime=600&name=sms";

        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //指定Content-Type：application/json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);



        restTemplate.exchange(url, HttpMethod.POST,null, MappingChange.Map.class);

    }


}
