package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-05 20:44
 */
public interface ScheduleService {

    void saveSchedule(Map<String, Object> stringObjectMap);

    Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap);

    void removeSchedule(Map<String, Object> stringObjectMap);

    Map<String, Object> page(Integer pageNume, Integer pageSzie, String hoscode, String depcode);

    List<Schedule> detail(String hoscode, String depcode, String workDate);

    Map<String, Object> getUserSchedulePage(String hoscode, String depcode, Integer pageNum, Integer pageSize);

    Schedule getScheduleInfo(String id);

    ScheduleOrderVo getScheduleById(String scheduleId);

    boolean updateAvailableNumber(String scheduleId, Integer availableNumber);

    void cancelSchedule(String scheduleId);
}
