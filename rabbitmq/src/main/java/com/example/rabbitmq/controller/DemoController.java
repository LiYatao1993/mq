package com.example.rabbitmq.controller;

import com.example.rabbitmq.broadcast.MQBroadCast;
import com.example.rabbitmq.lisner.ConfirmCallBackLister;
import com.example.rabbitmq.lisner.ReturnCallBackLister;
import com.rabbitmq.client.Return;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 18:06
 */
@RestController
@RequestMapping(value = "demo")
public class DemoController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping(value = "")
    public ResponseEntity demo(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "krystal-zheng");
        map.put("age", 26);
        rabbitTemplate.convertAndSend(MQBroadCast.MQ_EXCHANGE, MQBroadCast.MQ_ROUTING_KEY, map);
        return ResponseEntity.noContent().build();
    }
}
