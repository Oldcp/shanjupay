package com.shanjupay.transaction.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.transaction.api.TransactionService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author old money
 * @create 2022-03-21 16:48
 */
@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_RESULT",consumerGroup = "CID_ORDER_CONSUMER")
public class TransactionPayConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private TransactionService transactionService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        PaymentResponseDTO responseDTO = JSON.parseObject(jsonString, PaymentResponseDTO.class);

        String tradeNo = responseDTO.getTradeNo(); //支付宝或微信的订单号
        String outTradeNo = responseDTO.getOutTradeNo(); //闪聚平台订单号
        TradeStatus tradeState = responseDTO.getTradeState(); //交易状态

        switch (tradeState){
            case SUCCESS:
                //交易成功
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo,tradeNo,"2");
                return;
            case REVOKED:
                //交易关闭
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo,tradeNo,"4");
                return;
            case FAILED:
                //交易失败
                transactionService.updateOrderTradeNoAndTradeState(outTradeNo,tradeNo,"5");
                return;
            default:
                throw new RuntimeException("无法解析支付结果");
        }

    }
}
