package com.example.rabbitmq.config;

import com.example.rabbitmq.lisner.ConfirmCallBackLister;
import com.example.rabbitmq.lisner.ReturnCallBackLister;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 18:22
 */
@Configuration
public class RabbitConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private ConfirmCallBackLister confirmCallBackLister;

    @Autowired
    private ReturnCallBackLister returnCallBackLister;

    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback(confirmCallBackLister);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(returnCallBackLister);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    private MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
