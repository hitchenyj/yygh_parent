package com.atguigu.yygh.sms.service;

import com.atguigu.yygh.vo.msm.MsmVo;

/**
 * @author chenyj
 * @create 2022-12-10 11:23
 */
public interface SmsService {
    boolean sendCode(String phone);

    void sendMessage(MsmVo msmVo);
}
