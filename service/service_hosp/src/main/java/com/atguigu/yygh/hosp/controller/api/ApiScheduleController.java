package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-05 20:40
 */
@RestController
@RequestMapping("/api/hosp")
public class ApiScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //省略验证signKey
        scheduleService.saveSchedule(stringObjectMap);

        return Result.ok();
    }

    @PostMapping("/schedule/list")
    public Result<Page> getSchedulePage(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //省略验证signKey
        Page<Schedule> page = scheduleService.getSchedulePage(stringObjectMap);
        return Result.ok(page);
    }

    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //省略验证signKey
        scheduleService.removeSchedule(stringObjectMap);
        return Result.ok();
    }
}
