package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

/**
 * @author old money
 * @create 2022-03-20 17:52
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerSimpleTest {

    @Autowired
    public ProducerSimple producerSimple;

    @Test
    public void testSendSyncMsg(){
        producerSimple.sendSyncMsg("my-topic","第一条消息");
    }


    @Test
    public void testSendASyncMsg(){
        producerSimple.sendASyncMsg("my-topid","第一条异步消息");
    }

    @Test
    public void testSendMsgByJson(){

        OrderExt orderExt = new OrderExt();
        orderExt.setId("12");
        orderExt.setMoney(500l);
        orderExt.setTitle("123");
        producerSimple.sendMsgByJson("my-topic-obj",orderExt);
    }


    @Test
    public void testSendMsgByJsonDelay(){

        OrderExt orderExt = new OrderExt();
        orderExt.setId("12");
        producerSimple.sendMsgByJsonDelay("my-topic-obj",orderExt);
    }
}
