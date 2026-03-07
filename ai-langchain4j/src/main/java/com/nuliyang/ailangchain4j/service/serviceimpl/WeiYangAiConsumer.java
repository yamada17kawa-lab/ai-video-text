package com.nuliyang.ailangchain4j.service.serviceimpl;


import com.nuliyang.ailangchain4j.service.weiYangAiService;
import com.nuliyang.common.entity.WeiYangAiTask;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class WeiYangAiConsumer {

    @Autowired
    private weiYangAiService weiYangAiService;

    @RabbitListener(queues = "weiYangAiQueue")
    public void handleWeiYangAiTask(WeiYangAiTask task, Channel channel, Message message) throws IOException {
        try {
            log.info("消费喂养ai任务: {}", task);
            weiYangAiService.weiYangAi(task.getFileDto(), task.getResourceId());
            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            log.error("喂养ai处理任务失败", e);
        }
    }

}
