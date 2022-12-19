package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-08 13:50
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/{hoscode}/{depcode}/{workDate}")
    public R detail(@PathVariable String hoscode,
                    @PathVariable String depcode,
                    @PathVariable String workDate) {

        List<Schedule> list = scheduleService.detail(hoscode, depcode, workDate);
        return R.ok().data("list", list);
    }

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @GetMapping("/{pageNume}/{pageSzie}/{hoscode}/{depcode}")
    public R page(@PathVariable Integer pageNume, @PathVariable Integer pageSzie,
                  @PathVariable String hoscode, @PathVariable String depcode) {
        //这里应该返回Page对象，但是，还要返回其它信息，所有用Map
        Map<String, Object> map = scheduleService.page(pageNume, pageSzie, hoscode, depcode);

        return R.ok().data(map);
    }
}
