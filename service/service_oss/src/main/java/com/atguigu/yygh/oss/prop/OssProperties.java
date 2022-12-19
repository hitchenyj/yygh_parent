package com.atguigu.yygh.oss.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-11 22:49
 */
@Data
@ConfigurationProperties(prefix = "aliyun.oss.file")
@Component
//1. @PropertySource注解不支持yml文件 2.@PropertySource不能和主启动类上的@EnableConfigurationProperties(OssProperties.class)注解搭配使用
@PropertySource(value = {"classpath:oss.properties"})
public class OssProperties {

    private String endpoint;
    private String keyid;
    private String keysecret;
    private String bucketname;
}
