package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallOrderApplicationTests {

    /*
    * 1.如何创建exchange、queue、bingding
    * 1.1 使用AmqpAdmin创建
    *
    */

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessageTest() {

        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("test haha");

        String msg = "hello world";
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("消息发送完成{}", reasonEntity);

    }

    @Test
    void createExchange() {
        //amqpAdmin
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);

        amqpAdmin.declareExchange(directExchange);
        log.info("Exchage[{}]创建成功", "hello-java-exchange");
    }

    @Test
    void createQueues() {
//        Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("队列[{}]创建成功", "hello-java-queue");
    }

    @Test
    void createBinding() {
//        Binding(String destination[目的地],
//        Binding.DestinationType destinationType[目的地类型],
//        String exchange[交换机],
//        String routingKey[路由键],
//        @Nullable Map<String, Object> arguments[自定义参数])
        //讲exchange指定的交换机跟目的地进行绑定, 指定路由键
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE, "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("绑定[{}]创建成功");
    }
}
