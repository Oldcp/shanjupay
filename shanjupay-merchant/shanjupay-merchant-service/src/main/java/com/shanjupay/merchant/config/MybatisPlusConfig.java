package com.shanjupay.merchant.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mp 的分页配置插件
 *
 * @author old money
 * @create 2022-03-10 17:29
 */
@Configuration
@MapperScan("com.shanjupay.**.mapper")
public class MybatisPlusConfig {


    /**
     * 分页插件
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }




    /**
     * 启动性能分析插件
     * @return
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor(){
        return new PerformanceInterceptor();
    }




}
