package com.shanjupay.test.rocketmq.message;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author old money
 * @create 2022-03-20 17:57
 */
@Component
@RocketMQMessageListener(topic = "my-topic",consumerGroup = "demo-consumer-group")
public class ConsumerSimple implements RocketMQListener<String> {


    /**
     * 此方法被调用就表示接收到消息了，msg就表示消息的内容
     * @param msg
     */
    @Override
    public void onMessage(String msg) {

    }
}
