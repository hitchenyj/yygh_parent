package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.service.WeiPaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-16 12:47
 */
@RestController
@RequestMapping("/user/order/weixin")
public class WeiPayController {

    @Autowired
    private WeiPaiService weiPaiService;

    @GetMapping("/{orderId}") //通过订单id生成微信支付的二维码
    public R createNative(@PathVariable Long orderId)  {
        String url = weiPaiService.createNative(orderId);
        return R.ok().data("url", url);
    }

    @GetMapping("/status/{orderId}")
    public R getPayStatus(@PathVariable Long orderId) {
        Map<String, String> map = weiPaiService.queryPayStatus(orderId);
        if (map == null) { //说明出异常了
            return R.error().message("查询失败");
        }

        //走到这里，表示查询成功了
        if ("SUCCESS".equals(map.get("trade_state"))) { //支付成功
            weiPaiService.afetPaySuccess(orderId, map);

            return R.ok();
        }

        //没支付成功
        return R.ok().message("支付中"); //包含支付失败的情况
    }
}
