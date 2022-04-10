package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author old money
 * @create 2022-03-14 10:10
 */
@Api(value = "商户平台-应用管理",tags = "商户平台-应用相关",description = "商户平台-应用相关")
@RestController
public class AppController {

    @Reference
    private AppService appService;



    @ApiOperation("商户创建应用")
    @ApiImplicitParam(name = "appDTO",value = "应用信息",required = true,
                    dataType = "AppDTO",paramType = "body")
    @PostMapping("/my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO){

        //从Token中解析当前登录商户id
        Long merchantId = SecurityUtil.getMerchantId();

        return appService.createApp(merchantId,appDTO);
    }



    @ApiOperation("查询商户下的所有应用列表")
    @GetMapping("/my/apps")
    public List<AppDTO> queryMyApps(){

        //从Token中解析当前登录商户的id
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.queryAppByMerchant(merchantId);
    }




    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParam(name = "appId",value = "应用id",required = true,
                    dataType = "String",paramType = "path")
    @GetMapping("/my/apps/{appId}")
    public AppDTO getApp(@PathVariable("appId") String appId){

        return appService.getAppById(appId);
    }


}
