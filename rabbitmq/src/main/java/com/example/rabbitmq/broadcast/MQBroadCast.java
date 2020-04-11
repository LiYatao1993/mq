package com.example.rabbitmq.broadcast;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    //路由键
    public static final String MQ_ROUTING_KEY = "MQ_ROUTING_KEY";

    //创建交换机
    @Bean
    public Exchange getAttendanceMachineLogExchange() {
        return ExchangeBuilder.directExchange(MQ_EXCHANGE).durable(true).build();
    }

    //创建队列
    @Bean
    public Queue getAttendanceMachineQueue() {
        return new Queue(MQ_QUEUE,true);
    }

    //交换机和队列绑定
    @Bean
    public Binding bindAttendanceMachine() {
        return BindingBuilder.bind(getAttendanceMachineQueue()).to(getAttendanceMachineLogExchange()).with(MQ_ROUTING_KEY).noargs();
    }

}
