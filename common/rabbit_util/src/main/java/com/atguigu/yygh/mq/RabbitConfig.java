package com.atguigu.yygh.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author chenyj
 * @create 2022-12-15 21:21
 */
@SpringBootConfiguration
public class RabbitConfig {

    //作用: 将发送到RabbitMQ中的pojo对象自动转换json格式存储
    //     当从rabbit中消费消息的时候，也会自动把json格式的字符串转换为pojo对象类型
    @Bean //来一个Bean对象，把它放入到容器里
    public MessageConverter getMessageConverter() {

        return new Jackson2JsonMessageConverter();
    }
}
