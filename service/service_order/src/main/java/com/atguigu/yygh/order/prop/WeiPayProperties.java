package com.atguigu.yygh.order.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author chenyj
 * @create 2022-12-16 12:53
 */
// 而且，注意：@PropertySource 不能和 .yml文件一起使用，它也不能和主启动类 @EnableConfigurationProperties一起使用，
// 所以，只能使用 @Component 这种方式放入到容器中！
//使用@PropertySource注解，指定它的value属性，让它去类路径下加载weipay.properties文件
@PropertySource(value = "classpath:weipay.properties")
@ConfigurationProperties(prefix = "weipay") //它配置类去上面这个文件里以："weipay"开头的属性
@Component //想要让它去加载属性的话，必须把当前配置类放入到容器中，加@Component注解，
@Data //提供属性的getter/setter方法 和 toString方法
public class WeiPayProperties {

    private String appid;
    private String partner;
    private String partnerkey;
}
