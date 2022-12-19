package com.atguigu.yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author chenyj
 * @create 2022-12-06 17:44
 */

/*
    需要指定@FeignClient注解的value属性值为：被调用方在注册中心的应用名称
    在被调用方的配置文件application.properties中指定了：spring.application.name=service-cmn
 */
@FeignClient(value = "service-cmn")
public interface DictFeignClient {
    //根据医院所属的省市区编号，获取省市区文字
    @GetMapping("/admin/cmn/{value}")
    public String getNameByValue(@PathVariable("value") Long value); //value类型需要跟数据库中的类型保持一致

    //根据医院的等级编号，获取医院等级信息
    @GetMapping("/admin/cmn/{dictCode}/{value}")
    public String getNameByDictCodeAndValue(@PathVariable("dictCode") String dictCode, @PathVariable("value") Long value);
}
