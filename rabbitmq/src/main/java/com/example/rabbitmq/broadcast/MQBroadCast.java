package com.example.rabbitmq.broadcast;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 11:45
 */
@Configuration
public class MQBroadCast {
    //交换机
    public static final String MQ_EXCHANGE = "MQ_EXCHANGE";

    //队列
    public static final String MQ_QUEUE = "MQ_QUEUE";

    //交换机
    public static final String DLX_MQ_EXCHANGE = "DLX_MQ_EXCHANGE";

    //队列
    public static final String DLX_MQ_QUEUE = "DLX_MQ_QUEUE";

    //路由键
    public static final String MQ_ROUTING_KEY = "MQ_ROUTING_KEY";

    //创建交换机
    @Bean
    public Exchange getMQExchange() {
        return ExchangeBuilder.directExchange(MQ_EXCHANGE).durable(true).build();
    }

    //创建死信交换机
    @Bean
    public FanoutExchange getDLXMQExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_MQ_EXCHANGE).durable(true).build();
    }

    //创建队列
    @Bean
    public Queue getMQQueue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_MQ_EXCHANGE);
        return new Queue(MQ_QUEUE, true, false, false, args);
    }

    //创建死信队列
    @Bean
    public Queue getDLXMQQueue() {
        return new Queue(DLX_MQ_QUEUE, true);
    }

    //交换机和队列绑定
    @Bean
    public Binding bindMQ() {
        return BindingBuilder.bind(getMQQueue()).to(getMQExchange()).with(MQ_ROUTING_KEY).noargs();
    }

    //死信交换机和队列绑定
    @Bean
    public Binding bindDLXMQ() {
        return BindingBuilder.bind(getDLXMQQueue()).to(getDLXMQExchange());
    }

}
