package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author old money
 * @create 2022-03-21 16:03
 */
@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_ORDER",consumerGroup = "CID_PAYMENT_CONSUMER")
public class PayConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private PayChannelAgentService payChannelAgentService;

    @Autowired
    private PayProducer payProducer;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        //将消息解析为对象
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(jsonString, PaymentResponseDTO.class);

        String outTradeNo = paymentResponseDTO.getOutTradeNo(); //闪聚平台订单号
        String content = String.valueOf(paymentResponseDTO.getContent());
            AliConfigParam aliConfigParam = JSON.parseObject(content, AliConfigParam.class); //支付渠道参数

        PaymentResponseDTO responseDTO = null;
        if (paymentResponseDTO.getMsg().equals("ALIPAY_WAP")){
        //调用支付宝订单状态查询接口
            responseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, outTradeNo);

        }else if (paymentResponseDTO.getMsg().equals("WX_JSAPI")){
            WXConfigParam wxConfigParam = JSON.parseObject(content, WXConfigParam.class); //支付渠道参数
            responseDTO = payChannelAgentService.queryPayOrderByWeChat(wxConfigParam,outTradeNo);

        }
            //当没有获取到订单结果，抛出异常，再次重试消费
            if (responseDTO == null || TradeStatus.UNKNOWN.equals(responseDTO.getTradeState()) || TradeStatus.USERPAYING.equals(responseDTO.getTradeState())){
                throw new RuntimeException("状态未知请等待重试");
            }
            //如果重试的次数达到一定次数，不再重试消费，而是将消息记录到数据库，由单独的程序或人工进行处理.


        //将查询到的订单信息再次发送到MQ
        payProducer.payResultNottice(responseDTO);

    }
}
