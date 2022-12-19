package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-14 10:28
 */
@RestController
@RequestMapping("/user/hosp/schedule")
public class UserScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /*
        下面的getScheduleInfo是给前端用户返回的schedule R对象；
        在实际开发中不建议接口服用，建议的是：方法服用
        所以，这里单独提供一个接口，给order模块返回排班schedule信息
     */
    @GetMapping("/{scheduleId}")
    public ScheduleOrderVo getScheduleById(@PathVariable(value = "scheduleId") String scheduleId) {
        return scheduleService.getScheduleById(scheduleId);
    }

    //根据排班id获取排班信息
    @GetMapping("/info/{id}")
    public R getScheduleInfo(@PathVariable String id) {
        Schedule schedule = scheduleService.getScheduleInfo(id);
        return R.ok().data("schedule", schedule);
    }

    @GetMapping("/{hoscode}/{depcode}/{pageNum}/{pageSize}")
    public R getSchedulePage(@PathVariable String hoscode,
                             @PathVariable String depcode,
                             @PathVariable Integer pageNum,
                             @PathVariable Integer pageSize) {
        Map<String, Object> map = scheduleService.getUserSchedulePage(hoscode, depcode, pageNum, pageSize);
        return R.ok().data(map);
    }

    @GetMapping("/{hoscode}/{depcode}/{workdate}")
    public R detail(@PathVariable String hoscode,
                    @PathVariable String depcode,
                    @PathVariable String workdate) {
        List<Schedule> details = scheduleService.detail(hoscode, depcode, workdate);
        return R.ok().data("details", details);
    }
}
