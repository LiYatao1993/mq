package com.example.rabbitmq.service.impl;

import com.example.rabbitmq.dto.RabbitMessage;
import com.example.rabbitmq.service.IRabbitMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-11 14:29
 */
@Service
public class RabbitMessageServiceImpl implements IRabbitMessageService {

    @Override
    public RabbitMessage init(Message message) {
        MessageProperties messageProperties = message.getMessageProperties();
        return new RabbitMessage(new String(message.getBody()), messageProperties.getConsumerQueue(), messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey());
    }
}
