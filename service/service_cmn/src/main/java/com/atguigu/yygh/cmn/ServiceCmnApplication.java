package com.atguigu.yygh.cmn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author chenyj
 * @create 2022-11-30 20:35
 */
@SpringBootApplication
//编写主启动类之后一定要指定一个ComponentScan扫描包，它就能扫描当前模块下"com.atguigu"包下的那些加了注解的类；
// 同时它也能扫描当前模块所依赖的其它模块里面加了注解的类：它就能影响分页、全局异常处理、乐观锁等能否使用，都要求cmn模块依赖common.service_utils模块
@ComponentScan(basePackages = {"com.atguigu"})
// 这里还配置了扫描包，当我们写持久化层的时候，扫描这个mapper接口就可以了
@MapperScan("com.atguigu.yygh.cmn.mapper")
@EnableDiscoveryClient
public class ServiceCmnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
