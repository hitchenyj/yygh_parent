package com.atguigu.yygh.service.task.job;

import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.mq.RabbitService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-17 21:28
 */
@Component
public class PatientRemindJob {

    //因为要向rabbitmq发消息，注入rabbitmq工具类
    @Autowired
    private RabbitService rabbitService;

    /*
        Springboot中使用定时任务就两步:
            1. 在主启动类上加 @EnableScheduling 注解，开启定时任务支持
            2. 在当前 Job 类的方法上加一个 @Scheduled 注解

        Quartz：是一个基于SSM的定时任务框架
            cronExpression cron表达式

    相当于Quartz: cronExpression 秒 分 时 dayOfMonth Month dayOfWeek Year[最高到2099年]
     * : 表示任意xxx,
     ? : 表示无所谓
     - : 表示连续的时间段
     /n : 表示每隔多长时间
     , : 可以使用逗号隔开没有规律的时间
     # : 井号（#）：只能使用在周域上，用于指定月份中的第几周的哪一天，例如6#3，意思是某月的第三个周五 (6=星期五，3意味着月份中的第三周)
     L : 某域上允许的最后一个值。只能使用在日和周域上。当用在日域上，表示的是在月域上指定的月份的最后一天。用于周域上时，表示周的最后一天，就是星期六
     W : W 字符代表着工作日 (星期一到星期五)，只能用在日域上，它用来指定离指定日的最近的一个工作日
     */
//    @Scheduled(cron = "*/10 * * * * ?")
//    @Scheduled(cron = "1-5 * * * * ?")
    @Scheduled(cron = "*/30 * * * * ?")
    public void printTime() {
//        System.out.println(new DateTime().toString("yyyy-MM-dd HH:mm:ss"));

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
