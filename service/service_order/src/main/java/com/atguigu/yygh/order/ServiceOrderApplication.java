package com.atguigu.yygh.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.atguigu.yygh.order.mapper")//扫描持久化层的，需要创建一个mapper包
@ComponentScan(basePackages = {"com.atguigu"}) //让它不仅扫描当前模块，还要扫描当前模块所依赖的其它模块
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.atguigu"}) //远程调用要指定扫描的包
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}