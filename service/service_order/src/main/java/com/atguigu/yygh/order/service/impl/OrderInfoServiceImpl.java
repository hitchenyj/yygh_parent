package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.hosp.client.ScheduleFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.MqConst;
import com.atguigu.yygh.mq.RabbitConfig;
import com.atguigu.yygh.mq.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeiPaiService;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-12-14
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private ScheduleFeignClient scheduleFeignClient;
    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitConfig rabbitConfig;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private WeiPaiService weiPaiService;
    @Autowired
    private PaymentService paymentService;

    /*
        生成订单的过程：
                String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");
        String reserveDate = (String)paramMap.get("reserveDate");
        String reserveTime = (String)paramMap.get("reserveTime");
        String amount = (String)paramMap.get("amount");

     */
    @Override
    public Long submitOrder(String scheduleId, Long patientId) {
        //1. 先根据scheduleId获取医生排班信息
        ScheduleOrderVo scheduleOrderVo = scheduleFeignClient.getScheduleById(scheduleId);

        //2. 根据patientId获取就诊人信息
        Patient patient = patientFeignClient.getPatientById(patientId);
        DateTime dateTime = new DateTime(scheduleOrderVo.getStopTime()); //jodata-time依赖
        if (dateTime.isBeforeNow()) {
            throw new YyghException(20001, "超过了预约挂号截至时间");
        }

        //3. 从平台请求第三方医院，确认当前用户能否挂号
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", scheduleOrderVo.getReserveDate());
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount());
        paramMap.put("stopTime", scheduleOrderVo.getStopTime());

        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");
        if (jsonObject != null && 200 == jsonObject.getInteger("code")) {
            JSONObject data = jsonObject.getJSONObject("data");

            OrderInfo orderInfo = new OrderInfo();

            //schedule相关
            orderInfo.setUserId(patient.getUserId());
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo); //设置交易号，课件里用的是时间戳
            orderInfo.setHoscode(scheduleOrderVo.getHoscode());
            orderInfo.setHosname(scheduleOrderVo.getHosname());
            orderInfo.setDepcode(scheduleOrderVo.getDepcode());
            orderInfo.setDepname(scheduleOrderVo.getDepname());
            orderInfo.setTitle(scheduleOrderVo.getTitle());
            orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
            orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
            //patient相关
            orderInfo.setPatientId(patient.getId());
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            //
            orderInfo.setHosRecordId(data.getString("hosRecordId"));
            orderInfo.setNumber(data.getInteger("number"));
            orderInfo.setFetchTime(data.getString("fetchTime"));
            orderInfo.setFetchAddress(data.getString("fetchAddress"));
            //schedule
            orderInfo.setAmount(scheduleOrderVo.getAmount());
            orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

            //3.2 如果返回能挂号，就把医生排班信息、就诊人信息，以及第三方医院返回的信息都添加到order_info表中
            baseMapper.insert(orderInfo);

            //3.3 更新平台上，对应医生的剩余可预约数
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(data.getInteger("reservedNumber"));
            orderMqVo.setAvailableNumber(data.getInteger("availableNumber"));
            //3.4 给就诊人发送短信提醒
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            msmVo.setTemplateCode("您已经预约了上午${time}点的${name}医生的号，不要迟到!");
            Map<String, Object> msmMap = new HashMap<>();
            msmMap.put("time", scheduleOrderVo.getReserveDate() + " " + scheduleOrderVo.getReserveTime());
            msmMap.put("name", "xxx");
            msmVo.setParam(msmMap);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);


            //4. 返回订单的id: 返回保存订单的主键(id)
            //mybatisPlus往表中插入数据的时候，支持把表的主键赋值给orderInfo的id属性
            return orderInfo.getId();
        } else { //如果返回不能挂号，直接抛出异常
            throw new YyghException(20001, "号源已满");
        }
    }

    @Override
    public Page<OrderInfo> getOrderInfoPage(Integer pageNum, Integer pageSize, OrderQueryVo orderQueryVo) {
        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();

        Long userId = orderQueryVo.getUserId(); //用户id
        String outTradeNo = orderQueryVo.getOutTradeNo(); //订单号
        String keyword = orderQueryVo.getKeyword(); //医院名称，支持模糊查询
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate(); //预约日期
        String createTimeBegin = orderQueryVo.getCreateTimeBegin(); //下订单时间
        String createTimeEnd = orderQueryVo.getCreateTimeEnd(); //下订单时间

        if (!StringUtils.isEmpty(userId)) {
            queryWrapper.eq("user_id", userId);
        }
        if (!StringUtils.isEmpty(outTradeNo)) {
            queryWrapper.eq("out_trade_no", outTradeNo);
        }
        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper.like("hosname", keyword);
        }
        if (!StringUtils.isEmpty(patientId)) {
            queryWrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            queryWrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            queryWrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time", createTimeEnd);
        }
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(page, queryWrapper);
        orderInfoPage.getRecords().stream().forEach(item -> {
            this.packageOrderInfo(item);
        });
        return orderInfoPage;
    }

    @Override
    public OrderInfo detail(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        this.packageOrderInfo(orderInfo);
        return orderInfo;
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);

        //取消预约分为如下几步:
        //1. 确定当前当前取消预约的时间 和 订单中的退号截止时间(quit_time) 做一个比较; 看当前时间是否超过了规定的退号截止时间，
        // 1.1 如果超过了，直接抛出异常，不让用户取消
        DateTime dateTime = new DateTime(orderInfo.getQuitTime());
        if (dateTime.isBeforeNow()) {
            throw new YyghException(20001, "查过了退号的截止时间!");
        }

        //2. 如果没有超过截止时间，从平台请求第三方医院，通知第三方医院该用户已取消
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("hosRecordId", orderInfo.getHosRecordId());

        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/updateCancelStatus");
        // 2.1 第三方医院如果不同意取消，抛出异常，不能取消
        if (jsonObject == null || jsonObject.getIntValue("code") != 200) {
            throw new YyghException(20001, "取消失败");
        } // 2.2 第三方医院如果同意取消，继续往下执行

        //3. 判断用户是否对当前挂号订单是否已支付？
        if (orderInfo.getOrderStatus() == OrderStatusEnum.PAID.getStatus()) {
            // 3.1 如果已支付，退款
            Boolean flag = weiPaiService.refund(orderId);
            if (!flag) {
                throw new YyghException(20001, "退款失败！");
            }
        }
        // 无论用户是否进行了支付，都要继续往下执行
        //4. 更新订单状态, 支付记录表的支付状态，都改为已取消
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        baseMapper.updateById(orderInfo);
        //更新支付记录状态。
        UpdateWrapper<PaymentInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", orderId);
        updateWrapper.set("payment_status", PaymentStatusEnum.REFUND.getStatus());
        paymentService.update(updateWrapper);

        //5. 更新该医生的剩余可预约数信息
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        msmVo.setParam(null);
        orderMqVo.setMsmVo(msmVo);
        //6. 给就诊人发送短信提示
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
    }

    @Override
    public void patientRemind() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        queryWrapper.ne("order_status", OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> list = baseMapper.selectList(queryWrapper);
        for (OrderInfo orderInfo : list) {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS, MqConst.ROUTING_SMS_ITEM, msmVo);
        }
    }

    @Override
    public Map<String, Object> statistics(OrderCountQueryVo orderCountQueryVo) {
        /*
            注意，可以使用baseMapper中现成的查询方法，但是，现成的方法对于做统计都不好使。
            比如查询得到一个普通列表，不分组的时候用baseMapper还行，但是，如果涉及到分组最好自己写。
            MybatisPlus也支持在持久化层自定义方法。
         */
        List<OrderCountVo> countVoList = baseMapper.statistics(orderCountQueryVo);

        //注意这里不能使用并行流进行操作！
//        List<String> dateList = countVoList.stream().map(item -> item.getReserveDate()).collect(Collectors.toList());
        List<String> dateList = countVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList = countVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

//        List<String> dateList = new ArrayList<>();
//        List<Integer> countList = new ArrayList<>();
//        for (OrderCountVo orderCountVo : countVoList) {
//            String reserveDate = orderCountVo.getReserveDate();
//            Integer count = orderCountVo.getCount();
//            dateList.add(reserveDate);
//            countList.add(count);
//        }


        Map<String, Object> map = new HashMap<>();
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }

    private void packageOrderInfo(OrderInfo item) {
        item.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(item.getOrderStatus()));
    }
}
