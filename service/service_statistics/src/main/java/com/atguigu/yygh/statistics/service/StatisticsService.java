package com.atguigu.yygh.statistics.service;

import com.atguigu.yygh.client.OrderInfoFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-18 11:59
 */
@Service
public class StatisticsService {

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {

        return orderInfoFeignClient.statistics(orderCountQueryVo);
    }
}
