package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.prop.WeiPayProperties;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeiPaiService;
import com.atguigu.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import org.aspectj.weaver.ast.Var;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-16 14:24
 */
@Service
public class WeiPayServiceImpl implements WeiPaiService {

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private WeiPayProperties weiPayProperties;
    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    public String createNative(Long orderId) {
        //1. 根据订单id，去数据库中获取订单信息
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        //2. 保存支付记录信息
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        //3. 请求微信服务器，获取微信支付的url地址
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder"); //要请求微信服务器的哪个地址，才会给生成一个微信支付的地址
        Map<String, String> paramMap = new HashMap<>();

        paramMap.put("appid", weiPayProperties.getAppid()); //公众账号ID
        paramMap.put("mch_id", weiPayProperties.getPartner()); //商户号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串

        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateString + "就诊"+ orderInfo.getDepname();

        paramMap.put("body", body); //商品描述
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo()); //商户订单号
        paramMap.put("total_fee", "1"); //标价金额

        paramMap.put("spbill_create_ip", "127.0.0.1"); //终端IP,这种支付方式不会用到终端ip，随便写一个
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify"); //通知地址，也是给一个假的
        paramMap.put("trade_type", "NATIVE"); //交易类型

        try {
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey()));//设置参数
            httpClient.setHttps(true); //支持https协议
            httpClient.post(); //发送请求
            String xmlResult = httpClient.getContent();
            //XML字符串转换为MAP
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);

            //4. 将url返回给前端
            return resultMap.get("code_url");

        } catch (Exception e) {
            return "";
//            throw new YyghException(20001, "微信支付失败");
        }
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId) {

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weiPayProperties.getAppid()); //公众账号ID
        paramMap.put("mch_id", weiPayProperties.getPartner()); //商户号
        paramMap.put("out_trade_no", orderInfoService.getById(orderId).getOutTradeNo()); //商户订单号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
        //paramMap.put("sign", ""); //签名 : 微信申请时都设置为空串了，这里可以不用设置

        //MAP转换为XML字符串（自动添加签名）
        try {
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey()));
            httpClient.setHttps(true);//支持https协议
            httpClient.post();
            String content = httpClient.getContent();
            Map<String, String> stringStringMap = WXPayUtil.xmlToMap(content);

            return stringStringMap; //支付

        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    @Override
    public void afetPaySuccess(Long orderId, Map<String, String> map) {

        // 更新订单表的订单状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);

        // 更新支付记录表的支付状态
        UpdateWrapper<PaymentInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", orderId);
        updateWrapper.set("trade_no",map.get("transaction_id")); //微信支付订单号	transaction_id(微信服务器生成的)
        updateWrapper.set("payment_status", PaymentStatusEnum.PAID.getStatus());
        updateWrapper.set("callback_time", new Date());
        updateWrapper.set("callback_content", JSONObject.toJSONString(map));

        paymentService.update(updateWrapper);
    }

    @Override
    public Boolean refund(Long orderId) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);

        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
        if (refundInfo != null && refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
            //已退款
            return true;
        }
        //执行微信退款
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");

        Map<String,String> paramMap = new HashMap<>(8); //默认map开16个空间，代码优化，让它开8个就够了
        paramMap.put("appid", weiPayProperties.getAppid());       //公众账号ID
        paramMap.put("mch_id",weiPayProperties.getPartner());   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信支付订单号
        paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号：固定格式，可写死："tk" + out_trade_no
        //       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        //       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap,weiPayProperties.getPartnerkey());
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            httpClient.setCert(true); //设置证书支持
            httpClient.setCertPassword(weiPayProperties.getPartner());//设置证书密码：
            //httpClient.post();

//            String content = httpClient.getContent();
//            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("result_code", "SUCCESS");
            resultMap.put("refund_id", "1111111111111");
            if ("SUCCESS".equals(resultMap.get("result_code"))) {
                //退款成功，更新refund_info表
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackTime(new Date());
//                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
