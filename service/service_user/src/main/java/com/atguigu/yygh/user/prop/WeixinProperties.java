package com.atguigu.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-10 22:14
 */
/*
    获取application.properties中键对应值，并把它们绑定到pojo类对应的属性上，三种方式总结：
    1. @Component + @Value
    2. @Component + @ConfigurationProperties(prefix = "weixin")
    3. @ConfigurationProperties(prefix = "weixin") + @EnableConfigurationProperties(value = WeixinProperties.class)
 */
@ConfigurationProperties(prefix = "weixin") //让它默认去application.properties文件中加载以weixin开头的key（要求以weixin开头的键必须与属性名保持一致）
@Data //使用lombok提供get、set方法
//@Component //让这个类去配置文件中去加载，它怎么就知道去哪个文件中加载呢？————首先，要保证当前类必须在容器中（方案1:加@Component注解）
public class WeixinProperties {

    private String appid;
    private String scope;
    private String appsecret;
    private String redirecturl;
}
