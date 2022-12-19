package com.atguigu.yygh.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.atguigu"}) //配置扫描包，就是为了使用common模块那些公共配置类，包括swagger
//如果不排除的话，它默认连接数据库，但这里又没有配数据库连接信息，它默认就会连接本机上的数据，如果连接不上会报错，所以，排除数据源的自动配置
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class, args);
    }
}