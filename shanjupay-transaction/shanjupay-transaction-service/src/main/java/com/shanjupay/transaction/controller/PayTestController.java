package com.shanjupay.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * 支付宝接口对接的测试类
 * @author old money
 * @create 2022-03-16 17:57
 */
@Controller
@Slf4j
public class PayTestController {


    //应用id
    String APP_ID = "2021000119640099";
    //应用私钥
    String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC24gF0ajy4MNnCIPbR6ZfbifngaGfROmsTsl6McjfvWjf17Ajxh4I0kJoAfpkO7WqaSuyLW4v/glICgx/XtpO7I4CWQbsaB6Q5qFUXpj04UTCh03rrH5yNaFTuLI5rBSKt0pWvKqcxRSYidl7be2acRPAOqtAaf0Elfrrv611BA7ptqtAEopyAmRv0lU6hG6yYxDoP6P+/tC9wM8MxLdb9viI9qZmQXDts0H/GLv4p64uXjbMC53NEqo3GTme1t30zG547HzFmy/RTSBR+dwRfbUxfFN3jsL7Pxc38mbU1MiSWq1mYTRfuuc5lhWYPVSfsySvOJiiLOgRj4hdbyW+9AgMBAAECggEBAIetoqColIWeEF/aruLK55PDiHN5YdQy/++r/EM8Fc1oZG35gCUYgHvY77gANDOD5fu0LEuzTmjwWUFt4HjcBjvnOeW/vDyygJ8aCI80EOUwLuFQEThQyoeKaMAOpFY3iCPnQI+lGEcQYxtcMEGrClRTPLnU9fFCiaV0oOGP/y5UfYHVzu7U2BmurYzu4BSw9VVdbJzEQ7VcDf6/mnwgmWIL74yB4uWKccCNER+tNRshojL1hCxbYWyCkHBalPXazVXGEtsb0Z10+53fSiYVCIwqFfjFEfKbuGOqkM09MZERsT8KxviuY6EiD2g5aWtfoofjGg/+t/ASTi3AoAvTWiECgYEA/kuJ/PO9hF2y+0aWa2wcNSnhFjGBVaNmWNlM9ytiPDvhuMqISxmd5flvANb5+UfKENup0Xi1a/bkfPxd01Mp8c7uaaddnt/1c42eZoFijwqU//dsOU10Vp1qsfQKyarMkFufgy7VYUaUCF/hwpUJumuLHXbnnLaaHV1eGufEpIkCgYEAuBvl1ulYAgkTAHlyUm/c4RP2qz3f3JBXEPigRKfHeqrrNDtb0RNlytq/nP6vYkE+NGdCM40tidhHzlZEpczNCWaR8lmwE9gdJroop8t0TY/thZX+D94HZvEJL4FWSdgwYKzRHqtcIfbj9fR9yfD03OSarTqKHkHdsu5ddNRVTJUCfyu00AKp4ejeMeT+PNw5uJ9qk0U9PP17GBwtPQ0+Z6Pyptkb9lDyFjFwraN3T3+I1zIOjyFxnbzwslTJC1dG8+6bol6GZZDo7UbvEEqN8mKjbviPjgpLRbOdWrk7OJ9wu7gUB1aEtigB+W93C1ZmR0XDNiXaSfSlbdeeNI5rR2ECgYBUxBN4dYsdtMvY5hGqvvI1dxmVEjHlvVc46x/vPEhtxA/yxnmps4KkeT7Lq79YP63CNuCWF6Ql2TyGVdXZeIfWkjEUIhMdr90MHZ3mU5hCtceoP7zv1UwXK1cNVkFCUpVDjY3izwbmZGPktCLOPXCf22kT5OX+tFi8r168EZ8MWQKBgQDWeVTIGAFuaSmIGfBVHnxmF3we7ZRsh/xz3BVkwX7M6PdKyDeOxN9IgszEAwKOORtDDbOabFSFl5ZgDahLHBEvFG2xA/98V3M3RplbILBcD8UkTS/CPwxcjlR5ikYxFo42D8PU5A+m+Pp+13akQYjp80ejBayQBPb+cn8LjtOhxg==\n";
    //支付宝公钥
    String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkPPXh9ZuB5VdNU9P4nEAgm2MK2bcjx1BXQkbT3g5fUH9QiTZ+X+Aj7PhhgyKN7xOuWy8zFqmJJcsmU015E3KhAS0FWhv3huAWUMCy849U4kuRamLXqZHhfvQviwY5AnkhjEUZb37OM25NhNUvRNoozrqVzKnTwE20vJCp/LJJN5JuiVcW7BmkN1urW0TvYpZiRbjOWEIGLa4iNym+zAriBMLSYkRqtg9+DYOffbgGvsr3fW3xIESuAI42B8Sq5X5r81dhxUGhASIdFVLvLmH4vk6uPPhXiGIyjDq2hix9jt01z0QKDfLksGkV1fioJX0oenWaIE1VGYzv/0cRi3Q1QIDAQAB";
    String CHARSET = "utf-8";
    //支付宝接口的网关地址，正式"https://openapi.alipay.com/gateway.do"
    String serverUrl = "https://openapi.alipaydev.com/gateway.do";
    //签名算法类型
    String sign_type = "RSA2";

    @GetMapping("/alipaytest")
    public void alipaytest(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws ServletException, IOException {
        //构造sdk的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, sign_type); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150420010101017\"," +
                " \"total_amount\":\"88.88\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            //请求支付宝下单接口,发起http请求
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }



}
