package com.atguigu.yygh.hosp.listener;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.mq.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
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
 * @create 2022-12-15 22:37
 */
@Component
public class OrderMqListener {

    @Autowired
    private ScheduleService scheduleService;

    //给service_sms模块发消息，发送短信，所以，注入功能模块的RabbitService
    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = MqConst.QUEUE_ORDER, durable = "true"), //创建队列
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER, type = "direct"), //创建交换机
                    key = MqConst.ROUTING_ORDER
            )
//            @QueueBinding(),
//            @QueueBinding(),
    })
    //挂号成功，确认挂号：走该方法： 剩余可预约数做减法操作
    //取消预约，也走该方法：剩余可预约数做 +1
    //通过
    public void consume(OrderMqVo orderMqVo, Message message, Channel channel) {
        String scheduleId = orderMqVo.getScheduleId();
        Integer availableNumber = orderMqVo.getAvailableNumber();
        if (availableNumber != null) { //确认挂号
            boolean flag = scheduleService.updateAvailableNumber(scheduleId, availableNumber);


        } else {//取消预约
            scheduleService.cancelSchedule(scheduleId);
        }

        MsmVo msmVo = orderMqVo.getMsmVo();
        if (msmVo != null) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS, MqConst.ROUTING_SMS_ITEM, msmVo);
        }

//        message.getMessageProperties().getConsumerTag();

    }
}
