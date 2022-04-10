package com.shanjupay.test.rocketmq.message;


import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author old money
 * @create 2022-03-21 14:17
 */
@Component
@RocketMQMessageListener(topic = "my-topic-obj",consumerGroup = "demo-consumer-group-obj")
public class ConsumerSimpleObj implements RocketMQListener<MessageExt> {


    @Override
    public void onMessage(MessageExt messageExt) {

        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        System.out.println(jsonString);

        int reconsumeTimes = messageExt.getReconsumeTimes(); //消息的重试次数
        if (reconsumeTimes > 2){
            //将消息保存到数据库并直接返回
        }
        if (1 ==1){
            throw new RuntimeException("消息处理失败");
        }
    }
}
