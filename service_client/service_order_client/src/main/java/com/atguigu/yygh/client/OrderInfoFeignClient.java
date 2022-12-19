package com.atguigu.yygh.client;

import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-18 12:16
 */
@FeignClient(value = "service-order")
public interface OrderInfoFeignClient {
    @PostMapping("/api/order/orderInfo/statistic")
    public Map<String, Object> statistics(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
