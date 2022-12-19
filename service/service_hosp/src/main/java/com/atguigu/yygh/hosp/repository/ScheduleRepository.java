package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-05 20:50
 */
public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    Schedule findByHoscodeAndDepcodeAndHosScheduleId(String hoscode, String depcode, String hosScheduleId);

    Schedule findByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> findByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);

    Schedule findByHosScheduleId(String scheduleId);
}
