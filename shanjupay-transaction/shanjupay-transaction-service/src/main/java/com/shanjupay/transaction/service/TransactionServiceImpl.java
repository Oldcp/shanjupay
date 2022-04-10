package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @author old money
 * @create 2022-03-17 18:01
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {


    /**
     * 获取在 nacos 配置中心中配置的获取微信授权码的地址
     */
    @Value("${weixin.oauth2RequestUrl}")
    private String oauth2RequestUrl;


    /**
     * 授权码回调的地址
     */
    @Value("${weixin.oauth2CodeReturnUrl}")
    private String oauth2CodeReturnUrl;


    /**
     * 获取opentId的地址
     */
    @Value("${weixin.outh2Token}")
    private String oauth2Token;


    @Reference
    private AppService appService;

    @Reference
    private MerchantService merchantService;


    @Reference
    private PayChannelAgentService payChannelAgentService;


    @Autowired
    private PayOrderMapper payOrderMapper;


    @Autowired
    private PayChannelService payChannelService;

    //获取在交易服务配置文件中配置的支付入口url
    @Value("${shanjupay.url}")
    private String shanjupayUrl;

    /**
     * 生成门店二维码
     * @param qrCodeDto 传入merchantId,appId,storeid,channel,subject,body
     * @return
     * @throws BusinessException
     */
    @Override
    public String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException {

        //校验商户id和应用id和门店id的合法性
        //校验应用是否属于商户
        Boolean aBoolean = appService.queryAppInMerchant(qrCodeDto.getAppId(), qrCodeDto.getMerchantId());
        if (!aBoolean){
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        //校验门店是否属于商户
        Boolean aBoolean1 = merchantService.queryStoreInMerchant(qrCodeDto.getStoreId(), qrCodeDto.getMerchantId());
        if (!aBoolean1){
            throw new BusinessException(CommonErrorCode.E_200006);
        }

        //组装url所需的数据
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
        payOrderDTO.setAppId(qrCodeDto.getAppId());
        payOrderDTO.setStoreId(qrCodeDto.getStoreId());
        payOrderDTO.setSubject(qrCodeDto.getSubject());
        payOrderDTO.setChannel("shanju_c2b"); //服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());

        //转成JSON
        String jsonString = JSON.toJSONString(payOrderDTO);
        //base64编码
        String base64 = EncryptUtil.encodeUTF8StringBase64(jsonString);


        //目标是生成一个支付入口的url，需要携带参数将传入的参数转成json，用base64编码
        String url = shanjupayUrl + base64;
        return url;
    }


    /**
     * 保存支付宝的订单
     *  1.保存订单到闪聚平台
     *  2.调用支付渠道代理服务调用支付宝的接口
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {

        //保存订单到闪聚平台数据库
        payOrderDTO.setChannel("ALIPAY_WAP"); //支付渠道，支付宝
        PayOrderDTO save = save(payOrderDTO);

        //调用支付渠道代理服务，调用支付宝下单接口
        PaymentResponseDTO paymentResponseDTO = alipayH5(save.getTradeNo());
        return paymentResponseDTO;
    }



    /**
     *保存微信的订单
     *1.保存订单到闪聚平台
     * 2.调用支付渠道代理服务调用微信的接口
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException {
        //支付渠道
        payOrderDTO.setChannel("WX_JSAPI");

        //保存订单
        PayOrderDTO save = save(payOrderDTO);

        //调用支付渠道代理服务 微信下单
        Map<String, String> map = weChatJsapi(payOrderDTO.getOpenId(), payOrderDTO.getTradeNo());

        return map;
    }


    /**
     * 调用支付渠道代理服务的支付宝下单接口
     * @param tradeNo
     * @return
     */
    private PaymentResponseDTO alipayH5(String tradeNo){
        //订单信息，从数据库查询订单
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);

            //组装alipayBean(支付宝下单接口需要的业务参数)
            AlipayBean alipayBean = new AlipayBean();
            alipayBean.setOutTradeNo(payOrderDTO.getOutTradeNo());
        try {
            alipayBean.setTotalAmount(AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
            alipayBean.setSubject(payOrderDTO.getSubject());
            alipayBean.setBody(payOrderDTO.getBody());
            alipayBean.setTimestamp("30m"); //过期时间

        //支付渠道配置参数，从数据库查询
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "ALIPAY_WAP");
            //支付渠道参数是一串json串
        String param = payChannelParamDTO.getParam();
        AliConfigParam aliConfigParam = JSON.parseObject(param, AliConfigParam.class); //(支付宝下单接口需要的支付渠道参数)

        PaymentResponseDTO payOrderByAliWAP = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);

        return payOrderByAliWAP;
    }




    /**
     * 调用支付渠道代理服务的微信下单接口
     * @param openId
     * @param tradeNo
     * @return
     */
    private Map<String,String> weChatJsapi(String openId,String tradeNo){
        //查询订单信息
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);

        WeChatBean weChatBean = new WeChatBean();
        weChatBean.setOpenId(openId); //微信的openId
        weChatBean.setOutTradeNo(payOrderDTO.getTradeNo()); //闪聚平台订单号
        weChatBean.setTotalFee(payOrderDTO.getTotalAmount()); //金额
        weChatBean.setSpbillCreateIp(payOrderDTO.getClientIp()); //客户端的id
        weChatBean.setBody(payOrderDTO.getBody());
        weChatBean.setNotifyUrl("none");

        //支付渠道配置参数，从数据库查询
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "WX_JSAPI");
        //支付渠道参数是一串json串
        String param = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class); //(支付宝下单接口需要的支付渠道参数)


        Map<String, String> map = payChannelAgentService.createPayOrderByWeChatJSAPI(wxConfigParam, weChatBean);

        return map;
    }






    /**
     * 保存订单
     * @param payOrderDTO
     * @return
     */
    private PayOrderDTO save(PayOrderDTO payOrderDTO) throws BusinessException{

        PayOrder payOrder = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);

        payOrder.setTradeNo(PaymentUtil.genUniquePayOrderNo()); //订单号，采用雪花片算法
        payOrder.setCreateTime(LocalDateTime.now()); //创建时间
        payOrder.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES)); //过期时间
        payOrder.setCurrency("CNY"); //订单币种
        payOrder.setTradeState("0"); //订单状态 0 代表订单生成成功
        payOrderMapper.insert(payOrder);
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }





    /**
     * 更新支付订单状态
     * @param tradeNo 闪聚平台订单号
     * @param payChannelTradeNo 支付宝或微信的交易流水号
     * @param state 订单状态
     */
    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) throws BusinessException{
        UpdateWrapper<PayOrder> wrapper = new UpdateWrapper<>();
        wrapper.eq("tradeNo",tradeNo);
        wrapper.set("payChannelTradeNo",payChannelTradeNo);
        wrapper.set("tradeState",state);
        if (state != null && state .equals("2")){
            wrapper.set("paySuccessTime",LocalDateTime.now()); //更新订单支付时间
        }
        payOrderMapper.update(null,wrapper);
    }





    /**
     * 获取微信授权码
     * @param payOrderDTO
     * @return
     */
    @Override
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) {

        //闪聚平台的应用id
        String appId = payOrderDTO.getAppId();
        //获取微信支付渠道参数
        //String appId,String platformChannel,String payChannel
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        //微信支付渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
        //state是一个原样返回的参数
        String jsonString = JSON.toJSONString(payOrderDTO);
        String state = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
        try {
            String url = String.format("%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect",
                    oauth2RequestUrl,wxConfigParam.getAppId(), oauth2CodeReturnUrl,state
            );
            return "redirect:"+url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "forward:/pay-page-error";
    }




    /**
     *  申请openId
     * @param code 授权码
     * @param appId 闪聚平台的应用id，为了获取该应用的微信支付渠道参数
     * @return
     */
    @Override
    public String getWXOAuthOpenId(String code, String appId) {
        //获取微信支付渠道参数
        //String appId,String platformChannel,String payChannel
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        //微信支付渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
        //https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                oauth2Token,wxConfigParam.getAppId(), wxConfigParam.getAppSecret(), code);

        //申请openid，请求url
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //申请openid接口响应的内容，其中包括了openid
        String body = exchange.getBody();
        log.info("申请openid响应的内容:{}",body);
        //获取openid
        String openid = JSON.parseObject(body).getString("openid");
        return openid;
    }





    /**
     * 根据订单号查询订单信息
     * @param tradeNo
     * @return
     */
    public PayOrderDTO queryPayOrder(String tradeNo){
        QueryWrapper<PayOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("tradeNo",tradeNo);
        PayOrder payOrder = payOrderMapper.selectOne(wrapper);
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }


}
