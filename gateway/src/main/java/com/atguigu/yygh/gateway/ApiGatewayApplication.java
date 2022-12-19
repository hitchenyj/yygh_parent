package com.atguigu.yygh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//gateway底层是依赖webflux的；springmvc一套是基于sverlet的，一套是基于webflux[支持异步请求，多线程]的

@SpringBootApplication
public class ApiGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}