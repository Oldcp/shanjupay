package com.shanjupay.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * @author old money
 * @create 2022-03-10 17:50
 */
@RestController
@Api("商户平台应用的接口")
public class MerchantController {

    @Reference
    private MerchantService merchantService;

    @Autowired
    private SmsService smsService;

    @Resource
    private FileService fileService;


    @ApiOperation(value = "根据id查询商户信息")
    @ApiImplicitParam(name = "id",value = "商户id",required = true,
                    dataType = "Long")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id){
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }




    @ApiOperation("获取登录用户的商户信息")
    @GetMapping("/my/merchants")
    public MerchantDTO getMyMerchantInfo(){
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }






    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone",value = "手机号",required = true,
                    dataType = "String",paramType = "query")
    @GetMapping("/sms")
    public String getSMSCode(@RequestParam String phone){

        String sendMsg = smsService.sendMsg(phone);

        return sendMsg;
    }



    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegisterVO",value = "注册信息",required = true,
                    dataType = "MerchantRegisterVO",paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO){

        //校验参数的合法性
        if (merchantRegisterVO == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        if (StringUtils.isNotBlank(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        //校验手机号是否合法
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        //校验验证码
        smsService.checkVerifiyCode(merchantRegisterVO.getVerifiykey(),merchantRegisterVO.getVerifiyCode());

        //注册商户
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);

        return merchantRegisterVO;
    }


    /**
     * 上传证件照
     * @param multipartFile
     * @return
     */
    @ApiOperation("上传证件照")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传证件照") @RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {

        //生成的文件名称fileName，要保证他的唯一
            //上传的文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();

            //原始文件的扩展名
            String substring = originalFilename.substring(originalFilename.lastIndexOf(".") - 1);

            //文件名称
            String fileName = UUID.randomUUID() + substring;

        String upload = fileService.upload(multipartFile.getBytes(), fileName);

        return upload;
    }




    @ApiOperation("商户资质申请")
    @ApiImplicitParam(name = "merchantDetailVO",value = "商户认证资料",required = true,
                        dataType = "MerchantDetailVO",paramType = "body")
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantDetailVO){
        //解析token，取出当前登录商户的id
        Long merchantId = SecurityUtil.getMerchantId();

        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantDetailVO);

        merchantService.applyMerchant(merchantId,merchantDTO);
    }








}
