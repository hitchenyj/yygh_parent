package com.atguigu.yygh.common.config;

import com.google.common.base.Predicates;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.print.Doc;

/**
 * @author chenyj
 * @create 2022-11-25 19:19
 */
@SpringBootConfiguration
@EnableSwagger2 //开启Swagger支持
public class SwaggerConfig {

    /*
        swagger的几个注解：
        1. @Api(tags = ""): 标记在controller层的接口类上的，
        2. @ApiOperation(value = ""): 标记在controller中方法上的，
        3. @ApiParam(value = ""): 标记在controller中方法上的参数上的

        4. @ApiModel(description = ""): 标记在POJO类上的，对POJO类做说明的
        5. @ApiModelProperty(value = ""): 标记在POJO类的属性上的，对POJO类的属性做说明
     */

    //在配置类里提供一个Docket对象，然后把这个Docket对象放入到容器里
    //每个Docket对象对应一组
    @Bean
    public Docket getAdminDocket() {
//        return new Docket(DocumentationType.SWAGGER_2); //不分组
        //对Controller分组
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("admin")
                .apiInfo(getAdminApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();
    }

    @Bean
    public Docket getUserDocket() {
//        return new Docket(DocumentationType.SWAGGER_2); //不分组
        //对Controller分组
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("user")
                .apiInfo(getUserApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/user/.*")))
                .build();
    }

    @Bean
    public Docket getApiDocket() {
//        return new Docket(DocumentationType.SWAGGER_2); //不分组
        //对Controller分组
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Api")
                .apiInfo(getApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    //得到ApiInfo对象：ApiInfo对象是设置当前组的说明信息
    public ApiInfo getAdminApiInfo() {
        return new ApiInfoBuilder()
                .title("管理员系统")
                .description("尚医通预约挂号平台系统之管理员系统")
                .version("1.0")
//                .contact(new Contact("LH", "http://www.atguigu.com", "xxx@163.com"))
                .build();
    }

    //得到ApiInfo对象
    public ApiInfo getUserApiInfo() {
        return new ApiInfoBuilder()
                .title("普通用户系统")
                .description("尚医通预约挂号平台系统之普通用户系统")
                .version("1.0")
//                .contact(new Contact("LH", "http://www.atguigu.com", "xxx@163.com"))
                .build();
    }

    //得到ApiInfo对象
    public ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("第三方对接系统")
                .description("尚医通预约挂号平台系统之第三方对接系统")
                .version("1.0")
//                .contact(new Contact("LH", "http://www.atguigu.com", "xxx@163.com"))
                .build();
    }
}
