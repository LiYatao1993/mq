package com.example.rabbitmq.lisner;

import com.example.rabbitmq.broadcast.MQBroadCast;
import com.example.rabbitmq.service.IRabbitMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @RabbitListener(queues = MQBroadCast.MQ_QUEUE)
    public void monitoringMethod(Channel channel, Message message) throws IOException {
        //执行数据
        data(channel, message, "mq");
    }

    @Transactional
    @RabbitListener(queues = MQBroadCast.DLX_MQ_QUEUE)
    public void dlx(Channel channel, Message message) throws IOException {
        data(channel, message, "dlx_mq");
    }

    private void data(Channel channel, Message message, String data) throws IOException {
        try {
            Thread.sleep(3000);
            int i = 1 / 0;
            //消息确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            if (valueOperations.getOperations().hasKey("exception_" + data + "_" + new String(message.getBody()))) {
                valueOperations.getOperations().delete("exception_" + data + "_" + new String(message.getBody()));
            }
            log.info("------------------------->" + new String(message.getBody()));
        } catch (Exception e) {
            //失败后消息被确认
            if (valueOperations.getOperations().hasKey("exception_" + data + "_" + new String(message.getBody()))) {
                Integer count = (Integer) valueOperations.get("exception_" + data + "_" + new String(message.getBody()));
                if (count >= retryCount) {
                    if ("mq".equals(data)) {
                        //拒绝重新入队，配置死信队列后进入死信队列
                        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                    } else {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                        zSetOperations.add("mq_message", rabbitMessageService.init(message), zSetOperations.size("mq_message") + 1);
                    }
                    //超过最多次数
                    log.error("##############################" + new String(message.getBody()));
                    valueOperations.getOperations().delete("exception_" + data + "_" + new String(message.getBody()));

                } else {
                    valueOperations.set("exception_" + data + "_" + new String(message.getBody()), count + 1);
                    //重新入队
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                }
            } else {
                valueOperations.set("exception_" + data + "_" + new String(message.getBody()), 1);
                //重新入队4
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        }
    }
}
