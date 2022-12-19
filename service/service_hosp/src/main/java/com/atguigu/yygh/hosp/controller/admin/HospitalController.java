package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
/*
OpenFeign使用步骤：
1. 导入openFeign依赖；
2. 自定义一个Feign客户端接口，在这个自定义的Feign客户端接口上标一个@FeignClient注解，
   并给这个@FeignClient注解指定一个value属性值，这个value属性值应该设置为：被调用方在注册中心上的应用名称：
   @FeignClient(value="被调用方在注册中心上的应用名称")
   要求：自定义的Feign客户端接口里的方法必须和被调用方的Controller层方法完全一致（返回值、参数、请求路径、请求方式一致）
3. 在主启动类上加一个@EnableFeignClients注解
4. 在需要远程调用的地方，一般是在Service层，直接注入自定义Feign客户端接口的代理类对象，即可远程调用

 */
/**
 * @author chenyj
 * @create 2022-12-06 14:12
 */
@RestController
@RequestMapping("/admin/hospital")
//@CrossOrigin //改由gateway统一做跨域处理
public class HospitalController {

    @Autowired
    private HospitalService hospitalService; //注入Service层，跟对接第三方医院用同一个service即可

    @GetMapping("/{pageNume}/{pageSize}")
    public R getHospitalPage(@PathVariable Integer pageNume,
                             @PathVariable Integer pageSize,
                             HospitalQueryVo hospitalQueryVo) {
        //因为查询医院信息是 去mongodb里查，所以，只能使用Springdata里的Page，不能使用MybatisPlus，因为不是去数据表里查
        Page<Hospital> page = hospitalService.getHospitalPage(pageNume, pageSize, hospitalQueryVo);

        //在返回给前端分页数据的时候，只需要把总记录数、当前页的数据列表返回给前端；
        // 前端拿到总记录数和当前页的列表数据后，借助于element-ui里面的table组件和分页插件就能显示分页信息，和当前页列表数据
        return R.ok().data("total",page.getTotalElements()).data("list", page.getContent());
    }

    //根据医院id修改医院状态;
    // 只要涉及到后端跟前端交互的时候，返回的都是R对象
    // 如果参数比较少的话，建议使用路径占位符的方式传递；因为是修改操作，所以可以使用put请求：@PutMapping
    // 因为id是从mongodb里传过来的，id和status的类型是数据库里决定的，mongodb中id是String类型的,status是int类型
    @PutMapping("/{id}/{status}")
    public R updateStatus(@PathVariable String id, @PathVariable Integer status) {
        hospitalService.updateStatus(id, status);
        return R.ok();
    }

    //根据医院id获取医院所有信息
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable String id) {
        Hospital hospital = hospitalService.detail(id);

        return R.ok().data("hospital", hospital);
    }
}
