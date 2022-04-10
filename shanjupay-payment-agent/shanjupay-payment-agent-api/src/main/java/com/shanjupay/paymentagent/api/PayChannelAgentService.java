package com.shanjupay.paymentagent.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;

import java.util.Map;

/**
 * 与第三方支付渠道进行交互
 *
 * @author old money
 * @create 2022-03-18 18:07
 */
public interface PayChannelAgentService {


    /**
     *调用支付宝下单接口
     * @param aliConfigParam 支付渠道参数
     * @param alipayBean 交易业务参数
     * @return 统一返回PaymentResponseDTO
     */
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException;




    /**
     *查询支付宝的订单状态
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo 闪聚平台的订单号
     * @return
     */
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam,String outTradeNo);




    /**
     * 调用微信下单接口
     * @param wxConfigParam 微信支付渠道参数
     * @param weChatBean 微信支付业务参数
     * @return H5网页的数据
     * @throws BusinessException
     */
    public Map<String,String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean) throws BusinessException;



    /**
     * 查询微信的订单状态
     * @param wxConfigParam 支付渠道参数
     * @param outTradeNo 闪聚平台的订单号
     * @return
     * @throws BusinessException
     */
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam,String outTradeNo) throws BusinessException;

}
