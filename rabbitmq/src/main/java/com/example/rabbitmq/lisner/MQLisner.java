package com.example.rabbitmq.lisner;

import com.example.rabbitmq.broadcast.MQBroadCast;
import com.example.rabbitmq.service.IRabbitMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @AUTHOR yatao.li
 * @TIME 2020-04-09 11:49
 */
@Slf4j
@Component
public class MQLisner {

    @Value("${rabbitmq.publishe-to-exchange.retry.cout}")
    private Integer retryCount;

    @Autowired
    private ValueOperations valueOperations;

    @Autowired
    private ZSetOperations zSetOperations;

    @Autowired
    private IRabbitMessageService rabbitMessageService;

    @RabbitListener(queues = MQBroadCast.MQ_QUEUE)
    public void monitoringMethod(Channel channel, Message message) throws IOException {
        try {
            //消息确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            if (valueOperations.getOperations().hasKey("exception_mq_" + new String(message.getBody()))) {
                valueOperations.getOperations().delete("exception_mq_" + new String(message.getBody()));
            }
            log.info("------------------------->" + new String(message.getBody()));
        } catch (Exception e) {
            //失败后消息被确认
            if (valueOperations.getOperations().hasKey("exception_mq_" + new String(message.getBody()))) {
                Integer count = (Integer) valueOperations.get("exception_mq_" + new String(message.getBody()));
                if (count >= retryCount) {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                    //超过最多次数
                    log.error("##############################" + new String(message.getBody()));
                    valueOperations.getOperations().delete("exception_mq_" + new String(message.getBody()));

                    zSetOperations.add("mq_message", rabbitMessageService.init(message), zSetOperations.size("mq_message") + 1);
                } else {
                    valueOperations.set("exception_mq_" + new String(message.getBody()), count + 1);
                    //重新入队
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                }
            } else {
                valueOperations.set("exception_mq_" + new String(message.getBody()), 1);
                //重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        }
    }
}
