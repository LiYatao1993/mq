package com.example.rabbitmq.service;

import com.example.rabbitmq.dto.RabbitMessage;
import org.springframework.amqp.core.Message;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-11 14:28
 */
public interface IRabbitMessageService {

    RabbitMessage init(Message message);
}
