package com.atguigu.yygh.order.listener;

import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-17 22:56
 */
@Component
public class TaskListener {

    @Autowired
    private OrderInfoService orderInfoService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_TASK_8),
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_TASK),
                    key = MqConst.ROUTING_TASK_8
            )
    })
    public void patientRemind(Message message, Channel channel) {
        byte[] body = message.getBody();
        String s = new String(body);

        orderInfoService.patientRemind();
    }
}
