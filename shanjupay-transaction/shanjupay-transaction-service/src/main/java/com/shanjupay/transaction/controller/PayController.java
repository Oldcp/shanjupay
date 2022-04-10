package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.IPUtil;
import com.shanjupay.common.util.ParseURLPairUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 支付相关的Controller
 *
 * @author old money
 * @create 2022-03-18 16:28
 */
@Controller
@Slf4j
public class PayController {


    @Resource
    private TransactionService transactionService;

    @Reference
    private AppService appService;


    /**
     * 支付入口
     * @param ticket
     * @param request
     * @return
     */
    @RequestMapping("/pay-entry{ticket}")
    public String payEntry(@PathVariable("ticket") String ticket, HttpServletRequest request) throws Exception {

        //1.准备确认页面所需要的数据
        String stringBase64 = EncryptUtil.decodeUTF8StringBase64(ticket);

            //将JSON串转成对象
            PayOrderDTO payOrderDTO = JSON.parseObject(stringBase64, PayOrderDTO.class);

            //将对象的属性和值组成一个url的key/value串
            String parans = ParseURLPairUtil.parseURLPair(payOrderDTO);

        //2.解析客户端的类型(微信，支付宝)
            //得到客户端类型
            BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));
            switch (browserType){
                case ALIPAY:
                    //转发到确认页面
                    return  "forward:/pay-page?"+parans;
                case WECHAT:
                   //先获取授权码，申请openid，再到支付确认页面
                    return transactionService.getWXOAuth2Code(payOrderDTO);
                default:
            }

        //客户端类型不支持，转发到错误页面
        return  "forward:/pay-page-error";
    }




    /**
     * 授权码回调，申请获取授权码，微信将授权码请求到此地址
     * @param code
     * @param state
     * @return
     */
    @ApiOperation("微信授权码回调")
    @GetMapping("/wx-oauth-code-return")
    public String wxOAuth2CodeReturn(@RequestParam String code,@RequestParam String state) throws Exception {

        String jsonString = EncryptUtil.decodeUTF8StringBase64(state);
        PayOrderDTO payOrderDTO = JSON.parseObject(jsonString, PayOrderDTO.class);
        //闪聚平台的应用id
        String appId = payOrderDTO.getAppId();

        //获取授权码，申请openid
        String openId = transactionService.getWXOAuthOpenId(code, appId);

        //将对象的属性和值组成一个url的key/value串
        String parans = ParseURLPairUtil.parseURLPair(payOrderDTO);

        //转发到支付确认页面
        return "forward:/pay-page?openId="+openId + parans ;

    }



    /**
     * 支付宝下单接口，将前端订单确认页面的参数请求进来
     * @param orderConfirmVO
     * @param request
     * @param response
     * @throws BusinessException
     */
    @ApiOperation("支付宝门店下单付款")
    @PostMapping("/createAliPayOrder")
    public void createAlipayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request,
                                          HttpServletResponse response) throws BusinessException, IOException {
        //保存订单
        //调用支付渠道代理服务，实现第三方支付宝的下单
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);

        String appId = payOrderDTO.getAppId();
        AppDTO appById = appService.getAppById(appId);
        payOrderDTO.setMerchantId(appById.getMerchantId()); //设置商户id
        try {
            //将前端输入的元转换为分
            payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeF2Y(orderConfirmVO.getTotalAmount().toString())));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
        //客户端的id
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));

        PaymentResponseDTO<String> paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);

        //支付宝下单接口的响应
        response.getWriter().write(paymentResponseDTO.getContent());
        response.getWriter().flush();
        response.getWriter().close();
    }




    @ApiOperation("微信下单接口")
    @PostMapping("/wxjspay")
    public ModelAndView createWXOrderForStore(OrderConfirmVO orderConfirmVO,HttpServletRequest request){
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        String appId = payOrderDTO.getAppId();
        AppDTO app = appService.getAppById(appId);
        payOrderDTO.setMerchantId(app.getMerchantId());
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount().toString())));

        Map<String, String> map = transactionService.submitOrderByWechat(payOrderDTO);

        ModelAndView mv = new ModelAndView("wxpay",map);

        return mv;
    }

}
