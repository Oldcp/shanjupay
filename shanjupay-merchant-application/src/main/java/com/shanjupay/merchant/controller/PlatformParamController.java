package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户平台应用
 *
 * 平台支付参数配置相关的controller
 *
 * @author old money
 * @create 2022-03-14 14:40
 */
@RestController
@Api(value = "商户平台-渠道和支付参数相关",tags = "商户平台-渠道和支付参数",description = "商户平台-渠道和支付参数相关")
@Slf4j
public class PlatformParamController {

    @Reference
    private PayChannelService payChannelService;



    @ApiOperation("获取平台所有服务类型")
    @GetMapping("/my/platform-channels")
    public List<PlatformChannelDTO> queryPlatformChannel(){
        return payChannelService.queryPlatformChannel();
    }




    @ApiOperation("应用绑定服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId",value = "应用id",required = true,
                                dataType = "String",paramType = "path"),
            @ApiImplicitParam(name = "platformChannelCodes",value = "服务类型code",required = true,
                                dataType = "String",paramType = "query")
    })
    @PostMapping("/my/apps/{appId}/platform-channels")
    public void bindPlatformForApp(@PathVariable("appId") String appId,
                                   @RequestParam("platformChannelCodes") String platformChannelCodes){
        payChannelService.bindPlatformChannelForApp(appId,platformChannelCodes);

    }




    @ApiOperation("查看应用是否绑定了某个服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId",value = "应用id",required = true,
                            dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "platformChannel",value = "服务类型",required = true,
                            dataType = "String",paramType = "query")
    })
    @GetMapping("/my/merchants/apps/platformchannels")
    public int queryAppBindPlatformChannel(@RequestParam("appId") String appId,@RequestParam("platformChannel") String platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId, platformChannel);
    }



    @ApiOperation("根据平台的服务查询对应的支付渠道")
    @ApiImplicitParam(name = "platformChannelCode",value = "服务类型编码",required = true,
                        dataType = "String",paramType = "path")
    @GetMapping("/my/paychannels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable("platformChannelCode") String platformChannelCode){

       return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }




    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParam(name = "payChannelParamDTO",value = "商户配置支付渠道参数",required = true,
                        dataType = "PayChannelParamDTO",paramType = "body")
    @RequestMapping(value = "/my/pay-channel-params",method = {RequestMethod.POST,RequestMethod.PUT})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParamDTO) throws BusinessException{

        if (payChannelParamDTO == null || payChannelParamDTO.getParam() == null){
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        Long merchantId = SecurityUtil.getMerchantId(); //从当前登录用户的Token中获取商户id
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParamDTO);
    }





    @ApiOperation("获取指定应用指定服务类型下的支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId",value = "应用id",required = true,
                            dataType = "String",paramType = "path"),
            @ApiImplicitParam(name = "platformChannel",value = "平台服务类型",required = true,
                            dataType = "String",paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChanneParam(@PathVariable("appId") String appId,@PathVariable("platformChannel") String platformChannel){

        return payChannelService.queryChannelParamByAppAndPlatform(appId,platformChannel);
    }




    @ApiOperation("获取指定应用 指定服务类型 下的某个支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId",value = "应用Id",required = true,
                            dataType = "String",paramType = "path"),
            @ApiImplicitParam(name = "platformChannel",value = "服务类型",required = true,
                            dataType = "String",paramType = "path"),
            @ApiImplicitParam(name = "payChannel",value = "支付渠道代码",required = true,
                            dataType = "String",paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable("appId") String appId,@PathVariable("platformChannel") String platformChannel,@PathVariable("payChannel") String payChannel){

        return payChannelService.queryParamByAppPlatformAndPayChannel(appId,platformChannel,payChannel);
    }




}
