package com.example.rabbitmq.lisner;

import com.example.rabbitmq.service.IRabbitMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 17:15
 */
@Slf4j
@Component("returnCallBackListener")
public class ReturnCallBackLister implements RabbitTemplate.ReturnCallback {

    @Autowired
    private ZSetOperations zSetOperations;

    @Autowired
    private IRabbitMessageService rabbitMessageService;

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        //交换机保存到队列中失败
        log.error("交换机保存到队列中失败" + new String(message.getBody()));
        zSetOperations.add("mq_" + exchange, rabbitMessageService.init(message), zSetOperations.size("mq_" + exchange) + 1);
    }
}
