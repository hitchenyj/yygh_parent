package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-16 14:24
 */
public interface WeiPaiService {
    String createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    void afetPaySuccess(Long orderId, Map<String, String> map);

    Boolean refund(Long orderId);
}
