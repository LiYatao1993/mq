package com.example.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-11 13:56
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMessage {
    private String data;
    private String queue;
    private String exchange;
    private String routingKey;
}
