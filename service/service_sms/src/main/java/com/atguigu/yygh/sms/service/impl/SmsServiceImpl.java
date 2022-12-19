package com.atguigu.yygh.sms.service.impl;

import com.atguigu.yygh.sms.service.SmsService;
import com.atguigu.yygh.sms.utils.HttpUtils;
import com.atguigu.yygh.sms.utils.RandomUtil;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyj
 * @create 2022-12-10 11:24
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
//    private RedisTemplate<String, String> redisTemplate;//如果指定泛型，必须都指定相同类型的，不能一个String一个Object
    private RedisTemplate redisTemplate; //泛型也可以不指定，使用默认的Object的

    @Override
    public boolean sendCode(String phone) {
        //注意：开发中一般不这么做！
        //防止重复发送，如果redis中这个验证码还没过期，就直接返回，不再发送
        String redisCode = (String) redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(redisCode)) {
            return true;
        }

        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "fc666fb6a5854335a257a929b1ba32af"; //你自己的AppCode
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        String fourBitRandom = RandomUtil.getFourBitRandom();
        System.out.println("fourBitRandom = " + fourBitRandom);
        //需要注意的是: fourBitRandom做验证码的时候，它只能替换“code:”后面的1234, code:必须留下，否则发送报错
        querys.put("param", "code:" + fourBitRandom);
        querys.put("tpl_id", "TP1711063"); //固定的，不用动
        Map<String, String> bodys = new HashMap<String, String>();


        try {

            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));

            //把验证码保存redis中一份
            redisTemplate.opsForValue().set(phone, fourBitRandom, 500, TimeUnit.DAYS);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void sendMessage(MsmVo msmVo) {
        String phone = msmVo.getPhone();
        //阿里云发送短信提醒
        //模板，模板参数
        System.out.println("给就诊人发送短信提醒成功!");
    }
}
