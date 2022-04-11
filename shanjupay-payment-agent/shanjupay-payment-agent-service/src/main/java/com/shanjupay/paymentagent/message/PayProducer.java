package com.shanjupay.paymentagent.message;

import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author old money
 * @create 2022-03-21 15:55
 */
@Component
@Slf4j
public class PayProducer {

    int i = 1;


    @Resource
    private RocketMQTemplate rocketMQTemplate;


    //订单结果查询的 消息topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";


    //发送消息（查询订单状态）
    public void payOrderNotice(PaymentResponseDTO paymentResponseDTO){
        //构造消息
        Message<PaymentResponseDTO> message = MessageBuilder.withPayload(paymentResponseDTO).build();
        //发送延迟消息
        rocketMQTemplate.syncSend(TOPIC_ORDER,message,1000,3);
    }


    //订单结果的 消息topic
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";


    //发送消息 （支付结果）
    public void payResultNottice(PaymentResponseDTO paymentResponseDTO){
        rocketMQTemplate.convertAndSend(TOPIC_RESULT,paymentResponseDTO);
    }




}
