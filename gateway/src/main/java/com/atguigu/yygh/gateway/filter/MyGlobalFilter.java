package com.atguigu.yygh.gateway.filter;

import com.google.gson.JsonObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-07 16:24
 */
//@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /*
        从exchange交换机（也就是gateway中的路由）中获取请求报文
        注意：这个请求报文是ServerHttpRequest，不是以前的HttpServerletRequest.
        这是因为，以前学SpringMVC时，SpringMVC使用的是基于Servlet的方式，
        而gateway底层不是基于Servelet的方式，gateway的底层是基于webFlux的方式，webFlux请求报文类型是ServerHttpRequest
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {//执行过滤功能
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();//获取用户的请求路径
        //对请求路径进行判断，需要使用AntPathMatcher对象进行判断: 对于登录接口的请求，即以/admin/user开头的登录请求放行
        if (antPathMatcher.match("/admin/user/**", path)) { //match()中第一个参数是正则，第二个参数是path
            return chain.filter(exchange);
        } else { //对于非登录接口，验证：必须登录之后才能通过；没有登录的话，不允许访问
            //从请求报文中获取header
            List<String> strings = request.getHeaders().get("X-Token");
            if (StringUtils.isEmpty(strings)) {//拦截
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //路由跳转: 重定向到登录页面
                response.getHeaders().set(HttpHeaders.LOCATION, "http://localhost:9528");
                return response.setComplete(); //结束请求

//                //注意：使用的是Google的gson包下的JsonObject
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("success", false);
//                jsonObject.addProperty("code", 28004);
//                jsonObject.addProperty("data", "鉴权失败");
//                //先把jsonObject转换字节数组
//                byte[] bytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
//                //再转换成dataBuffer类型
//                DataBuffer buffer = response.bufferFactory().wrap(bytes);
//                //写数据之前需要先设置一下头信息: 指定编码，否则在浏览器中会中文乱码
//                response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
//                return response.writeWith(Mono.just(buffer));

//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                return response.setComplete();//结束请求
            } else { //放行
                return chain.filter(exchange);
            }
        }
    }

    //order影响的是全局过滤器的执行顺序: order值越小，优先级越高。
    @Override
    public int getOrder() {
        return 0;
    }
}
