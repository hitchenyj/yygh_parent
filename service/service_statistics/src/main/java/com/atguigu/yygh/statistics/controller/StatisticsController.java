package com.atguigu.yygh.statistics.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.statistics.service.StatisticsService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-18 11:52
 */
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/countByDate")
    public R statistics(OrderCountQueryVo orderCountQueryVo) { //上面使用的是 @GetMapping，所以，参数不能是json，只是普通的pojo对象
        Map<String, Object> map = statisticsService.statistics(orderCountQueryVo);
        return R.ok().data(map);
    }
}
