package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author chenyj
 * @create 2022-12-17 11:36
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {

        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        if (refundInfo != null) {
            return refundInfo;
        }

        refundInfo = new RefundInfo();
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setPaymentType(PaymentTypeEnum.WEIXIN.getStatus());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setSubject("想退款...");
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());

        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
