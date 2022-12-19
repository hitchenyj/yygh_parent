package com.atguigu.yygh.sms.listener;

import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.sms.service.SmsService;
import com.atguigu.yygh.vo.msm.MsmVo;
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
 * @create 2022-12-15 23:09
 */
@Component
public class SmsMqListener {

    @Autowired
    private SmsService smsService;

    //它要监听队列里的消息，前提是：这个队列和交换机必须存在
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_SMS_ITEM),
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_SMS),
                    key = MqConst.ROUTING_SMS_ITEM
            )
    })
    public void consumer(MsmVo msmVo, Message message, Channel channel) {
        smsService.sendMessage(msmVo);
    }
}
