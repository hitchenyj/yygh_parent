package com.atguigu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author chenyj
 * @create 2022-11-25 16:41
 */
@SpringBootApplication
@ComponentScan(value = "com.atguigu.yygh")
@MapperScan(value = "com.atguigu.yygh.hosp.mapper")
@EnableDiscoveryClient //Eureka: @EnableEurekaClient
//basePackages属性值指定它要扫描的包:
// 因为如果不指定，默认只扫描当前微服务里面和这个主启动类在同一个包底下的加了注解的Feign客户端接口；
//就跟@ComponentScan的原理是一样的
@EnableFeignClients(basePackages = "com.atguigu.yygh")
public class ServiceHospMainStarter {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospMainStarter.class, args);
    }
}
