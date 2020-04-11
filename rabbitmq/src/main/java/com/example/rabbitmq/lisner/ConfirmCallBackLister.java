package com.example.rabbitmq.lisner;

import com.example.rabbitmq.broadcast.MQBroadCast;
import com.example.rabbitmq.service.IRabbitMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 17:12
 */
@Slf4j
@Component("confirmCallBackLister")
public class ConfirmCallBackLister implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private IRabbitMessageService rabbitMessageService;

    @Autowired
    private ValueOperations valueOperations;

    @Autowired
    private ZSetOperations zSetOperations;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.publishe-to-exchange.retry.cout}")
    private Integer retryCount;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            //消息保存到交换机
            log.info("信息添加到交换机");
            if (valueOperations.getOperations().hasKey("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()))) {
                valueOperations.getOperations().delete("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()));
            }
        }else{
            //消息保存到交换机失败
            log.error("信息保存到交换机失败" + correlationData.getReturnedMessage());

            if (valueOperations.getOperations().hasKey("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()))) {
                Integer count = (Integer) valueOperations.get("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()));
                if (count >= retryCount) {
                    //超过最多次数
                    Map<String, Object> map = new HashMap<>();
                    map.put("cause", cause);
                    map.put("message", rabbitMessageService.init(correlationData.getReturnedMessage()));
                    zSetOperations.add("mq", map, zSetOperations.size("mq") + 1);
                } else {
                    valueOperations.set("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()), count + 1);
                    //重新入队
                    rabbitTemplate.convertAndSend(correlationData.getReturnedMessage().getMessageProperties().getReceivedExchange(), correlationData.getReturnedMessage().getMessageProperties().getReceivedRoutingKey(), correlationData.getReturnedMessage().getBody());
                }
            } else {
                valueOperations.set("confirm_mq_" + new String(correlationData.getReturnedMessage().getBody()), 1);
                //重新入队
                rabbitTemplate.convertAndSend(correlationData.getReturnedMessage().getMessageProperties().getReceivedExchange(), correlationData.getReturnedMessage().getMessageProperties().getReceivedRoutingKey(), correlationData.getReturnedMessage().getBody());
            }

        }
    }
}
