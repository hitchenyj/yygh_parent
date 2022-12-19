package com.atguigu.yygh.order.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jdk.nashorn.internal.parser.Token;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-12-14
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @GetMapping("/cancel/{orderId}")
    public R cancelOrder(@PathVariable Long orderId) {

        orderInfoService.cancelOrder(orderId);

        //这里上面取消操作只要不出异常，就让它return R.ok(); 如果出了异常，有全局的统一异常处理，也会返回前端R.error()
        return R.ok();
    }

    @GetMapping("/detail/{orderId}")
    public R detail(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderInfoService.detail(orderId);
        return R.ok().data("orderInfo", orderInfo);
    }

    @GetMapping("/statuslist")
    public R getStatusList() {
        List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
        return R.ok().data("statusList", statusList);
    }

    @GetMapping("/{pageNum}/{pageSize}")
    public R getOrderInfoPage(OrderQueryVo orderQueryVo,
                              @PathVariable Integer pageNum,
                              @PathVariable Integer pageSize,
                              @RequestHeader String Token) {
        Long userId = JwtHelper.getUserId(Token);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> page = orderInfoService.getOrderInfoPage(pageNum, pageSize, orderQueryVo);
        return R.ok().data("page", page);
    }

    @PostMapping("/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderInfoService.submitOrder(scheduleId, patientId);
        return R.ok().data("orderId", orderId);
    }

    /*
    注意：
        如果一个微服务被前端调用，返回的是一个R对象；
        而现在order微服务的这个接口是被service-statistics调用，
        所以，service-statistics需要什么数据，在service-order里直接返回什么数据就可以了；就不用再返回R对象了。

        service-statistics微服务也会把查询条件传给service-order这个微服务，所以，在这里做个接收，还是用OrderCountQueryVo orderCountQueryVo
    注意：在使用OpenFeign在进行远程调用时，它底层传递的数据都是json数据格式的，所以，这里参数前面必须加@RequestBody注解；
        因为@RequestBody注解没法跟@GetMapping一起使用，所以，必须把 @GetMapping 改为: @PostMapping
     */
//    @GetMapping("/statistic")
    @PostMapping("/statistic")
    public Map<String, Object> statistics(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderInfoService.statistics(orderCountQueryVo);
    }
}

