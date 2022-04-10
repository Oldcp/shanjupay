package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.QRCodeUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 门店管理相关的接口定义
 *
 * @author old money
 * @create 2022-03-17 17:20
 */
@Api(value = "商户平台-门店管理",tags = "商户门店-门店管理",description = "商户平台-门店的增删改查")
@RestController
@Slf4j
public class StoreController {

    @Resource
    private QRCodeUtil qrCodeUtil;

    @Reference
    private MerchantService merchantService;

    @Reference
    private TransactionService transactionService;


    //从配置中心中获取配置的标题属性
    @Value("${shanjupay.c2b.subject}")
    private String subject;


    //从配置中心中获取配置的body属性
    @Value("${shanjupay.c2b.body}")
    private String body;


    @ApiOperation("分页条件查询商户下门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo",value = "页码",required = true,
                    dataType = "Integer",paramType = "query"),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true
                            ,dataType = "Integer",paramType = "query")
    })
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestParam Integer pageNo,@RequestParam Integer pageSize){

        //商户id
        Long merchantId = SecurityUtil.getMerchantId();

        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantId);

        return merchantService.queryStoreByPage(storeDTO,pageNo,pageSize);
    }




    @ApiOperation("生成商户应用门店二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId",value = "门店id",required = true,
                            dataType = "Long",paramType = "path"),
            @ApiImplicitParam(name = "appId",value = "应用id",required = true,
                            dataType = "String",paramType = "path")
    })
    @GetMapping("/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable("storeId") Long storeId,@PathVariable("appId") String appId) throws IOException {

        //从当前登录用户token中获取商户id
        Long merchantId = SecurityUtil.getMerchantId();

        QRCodeDto qrCodeDto = new QRCodeDto();
        qrCodeDto.setMerchantId(merchantId);
        qrCodeDto.setStoreId(storeId);
        qrCodeDto.setAppId(appId);


        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        //标题，用商品名称替换 %s
        String subjectFormat = String.format(subject, merchantDTO.getMerchantName());
        //内容，用商品名称替换 %s
        String bodyFormat = String.format(body, merchantDTO.getMerchantName());

        qrCodeDto.setSubject(subjectFormat);
        qrCodeDto.setBody(bodyFormat);

        //获取二维码的URL
        String storeQRCode = transactionService.createStoreQRCode(qrCodeDto);

        //调用工具类生成二维码的图片
        String qrCode = qrCodeUtil.createQRCode(storeQRCode, 200, 200);
        return qrCode;
    }


}
