package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author old money
 * @create 2022-03-20 17:46
 */
@Component
public class ProducerSimple {

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 发送同步消息
     * @param topic
     * @param msg
     */
    public void sendSyncMsg(String topic,String msg){
        rocketMQTemplate.syncSend(topic,msg);
    }




    /**
     * 发送异步消息
     * @param topic
     * @param msg
     */
    public void sendASyncMsg(String topic,String msg){
        rocketMQTemplate.asyncSend(topic, msg, new SendCallback() {

            /**
             * 消息发送成功的回调
             * @param sendResult
             */
            @Override
            public void onSuccess(SendResult sendResult) {

            }

            /**
             * 消息发送失败的回调
             * @param throwable
             */
            @Override
            public void onException(Throwable throwable) {

            }
        });
    }


    /**
     * 同步发送对象消息
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJson(String topic, OrderExt orderExt){
        //将对象转换成json串发送
        rocketMQTemplate.convertAndSend(topic,orderExt);
    }




    /**
     *发送延迟消息
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJsonDelay(String topic,OrderExt orderExt){

        //构建消息体
        Message<OrderExt> message = MessageBuilder.withPayload(orderExt).build();

        //timeout:发送消息超时时间，毫秒 ，delaylevel:延迟等级
        rocketMQTemplate.syncSend(topic,message,1000,3);

    }


}
