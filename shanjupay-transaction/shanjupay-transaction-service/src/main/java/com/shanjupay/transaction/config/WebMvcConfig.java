package com.shanjupay.transaction.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 *
 * 配置支付入口的url 可以映射到支付确认的html页面
 *
 * @author old money
 * @create 2022-03-18 16:24
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/pay-page").setViewName("pay");
        registry.addViewController("/pay-page-error").setViewName("pay_error");
    }
}
