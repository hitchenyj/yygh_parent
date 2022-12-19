package com.atguigu.yygh.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-15 21:18
 */
@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendMessage(String ex, String routingkey, Object message) {

        rabbitTemplate.convertAndSend(ex, routingkey, message);
        return true;
    }
}
