package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenyj
 * @create 2022-12-05 20:44
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> stringObjectMap) {
        //借助FastJson工具类把这个stringObjectMap转换为Schedule对象
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Schedule.class);
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();
        String hosScheduleId = schedule.getHosScheduleId();

        Schedule platformSchedule = scheduleRepository.findByHoscodeAndDepcodeAndHosScheduleId(hoscode, depcode, hosScheduleId);
        if (platformSchedule == null) {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        } else {
            schedule.setCreateTime(platformSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(platformSchedule.getIsDeleted());

            schedule.setId(platformSchedule.getId());
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> getSchedulePage(Map<String, Object> stringObjectMap) {
        String hoscode = (String) stringObjectMap.get("hoscode");
        Integer pageNum = Integer.parseInt(stringObjectMap.get("page").toString());
        Integer pageSize = Integer.parseInt(stringObjectMap.get("limit").toString());

        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        Example<Schedule> example = Example.of(schedule);
        Pageable pageable = PageRequest.of(pageNum-1, pageSize, Sort.by("createTime").ascending());
        Page<Schedule> page = scheduleRepository.findAll(example, pageable);

        return page;
    }

    @Override
    public void removeSchedule(Map<String, Object> stringObjectMap) {
        String hoscode = (String) stringObjectMap.get("hoscode");
        String hosScheduleId = (String) stringObjectMap.get("hosScheduleId");

        //首先根据医院编号和排班号到mongodb里查询一下，获得id；再根据id删除
        Schedule schedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        } else {
            throw new YyghException(20001, "要删除的排班不存在");
        }

    }

    @Override
    public Map<String, Object> page(Integer pageNume, Integer pageSzie, String hoscode, String depcode) {

        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);//查询条件: 根据医院编号，科室编号
        //mongodb的聚合：按日期分组，排序；聚合最好用mongoTemplate
        Aggregation aggregation = Aggregation.newAggregation( //聚合条件
                Aggregation.match(criteria), //查询
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"), //排序
                Aggregation.skip((pageNume - 1) * pageSzie),
                Aggregation.limit(pageSzie)
        );
        /*
            三个参数：
            1. Aggregation aggregation; 表示聚合条件
            2. InputType：表示输入类型，可以根据当前指定的字节码找到mongodb中对应的集合
            3. OutputType：表示输出类型，可以封装聚合后的信息
         */
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //通过mongodb聚合后的结果，拿到当前页对应的排班列表数据: mappedResults
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        for (BookingScheduleRuleVo bookingScheduleRuleVo : mappedResults) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //工具类: Date -> 周几
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(workDate)));
        }

        Aggregation aggregation2 = Aggregation.newAggregation( //聚合条件
                Aggregation.match(criteria), //查询
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate2 = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        //先做分页,返回给前端的是总记录数和当前页列表数据
        Map<String, Object> map = new HashMap<>();
        map.put("list", mappedResults);
        map.put("total", aggregate2.getMappedResults().size());

        //获取医院名称
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hospital.getHosname());
        map.put("baseMap",baseMap);

        return map;
    }

    @Override
    public List<Schedule> detail(String hoscode, String depcode, String workDate) {

        Date date = new DateTime(workDate).toDate();
        List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);

        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });

        return scheduleList;
    }

    /*
    service层这个方法做的就是：
    带查询条件的、排班、聚合数据的分页
    查询条件：某个医院下、某个科室下、工作在预约周期内的、并且是在当前页的 这些数据
    最终，在map中封装了这些数据：
    {
        total:总记录数
        list: 当前页对应的列表
        baseMap:
    }
     */
    @Override
    public Map<String, Object> getUserSchedulePage(String hoscode, String depcode, Integer pageNum, Integer pageSize) {
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(20001, "该医院信息不存在");
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期分页数据
        IPage<Date> page = this.getListDate(pageNum, pageSize, bookingRule);
        List<Date> records = page.getRecords();

        /*
        根据inputType的字节码，找mongodb中的某个集合: Schedule
        查询后得到的数据封装到: BookingScheduleRuleVo 对象中
        Aggregation聚合条件
        Aggregation.group("workDate").first(): 所以mongodb中，分组 group之后，要想查询这个分组字段，必须用first，还可以给workDate起别名
            相当于sql查询中的：
            select 分组字段, sum(*), count(*)...
            from 表名
            where 字段名 = xx
            group by 分组字段
            一旦使用group 分组之后，select后面就不能写 * 了，应该写：分组字段 或 聚合函数(sum, avg)

            然后对总的“可预约数”求和
        */
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(records);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();//获取聚合后的排班列表
        //把排班列表转化成一个map
        Map<Date, BookingScheduleRuleVo> collect = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));

        int size = records.size();

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();

        for (int i = 0; i <size ; i++) {
            Date date = records.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = collect.get(date);
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setWorkDate(date);

                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1); //当天所有医生的总的剩余可预约数：前端页面是 -1表示没有医生
                bookingScheduleRuleVo.setReservedNumber(0);

            }
            bookingScheduleRuleVo.setWorkDateMd(date);
            bookingScheduleRuleVo.setDayOfWeek(getDayOfWeek(new DateTime(date)));

            //status控制的是前端是否显示停止挂号：状态 0：正常 1：即将放号 -1：当天已停止挂号
            bookingScheduleRuleVo.setStatus(0);

            //对第一页的第一条数据做特殊判断处理
            if(i == 0 && pageNum == 1) {
                DateTime dateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                //如果医院规定的当前挂号截至时间在此时此刻之前，说明此时此刻已经过了当天的挂号截至时间了
                if (dateTime.isBeforeNow()) {
                    bookingScheduleRuleVo.setStatus(-1); //仅仅适用第一条数据
                }
            }
            //对最后一页的最后一条数据做特殊判断处理
            if (pageNum == page.getPages() && i == (size - 1)) { //page.getPages()获取总页数
                bookingScheduleRuleVo.setStatus(1); //1：即将放号
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("list", bookingScheduleRuleVoList);

        Map<String, Object> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospitalByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());

        map.put("baseMap", baseMap);

        return map;
    }

    @Override
    public Schedule getScheduleInfo(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleById(String scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        BeanUtils.copyProperties(schedule, scheduleOrderVo);
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());

        DateTime dateTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(hospital.getBookingRule().getQuitDay()).toDate(),
                                                hospital.getBookingRule().getQuitTime());
        scheduleOrderVo.setQuitTime(dateTime.toDate());//设置预约的退号截至时间

        scheduleOrderVo.setStopTime(this.getDateTime(schedule.getWorkDate(), hospital.getBookingRule().getStopTime()).toDate());

        return scheduleOrderVo;
    }

    @Override
    public boolean updateAvailableNumber(String scheduleId, Integer availableNumber) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        schedule.setAvailableNumber(availableNumber);
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
        return true;
    }

    @Override
    public void cancelSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findByHosScheduleId(scheduleId);
        schedule.setAvailableNumber(schedule.getAvailableNumber() + 1);
        scheduleRepository.save(schedule);
    }

    //获取可预约日期分页数据方法
    private IPage<Date> getListDate(Integer pageNum, Integer pageSize, BookingRule bookingRule) {
        Integer cycle = bookingRule.getCycle();
        //判断此时此刻是否已经超过了医院规定的当天挂号起始时间，如果此时此刻已经超过了：cycle + 1
        String releaseTime = bookingRule.getReleaseTime();//先取出医院规定的当前挂号起始时间
        //拿到今天的日期，用今天的日期再拼上上面的挂号起始时间，就是医院规定这一天的挂号起始时间；调用方法转换成DateTime
        DateTime dateTime = this.getDateTime(new Date(), releaseTime); //今天医院规定的挂号的起始时间："yyyy-MM-dd HH:mm"

        //用上面拿到的时间 和 此时此刻(Now)的时间做一个比较
        if (dateTime.isBeforeNow()) {
            cycle+=1; //cycle就是可以预约的时间天数，有可能是10天，也可能是11天，相当于：时间列表的总记录数 total
        }
        //然后，准备从今天开始到预约周期内的所有的时间列表数据【10天 或 11天的】
        List<Date> list = new ArrayList<>();
        for (int i=0; i<cycle; i++) {
            //DateTime dateTime1 = new DateTime().plusDays(i); 尽量不要再for循环内部定义变量！所以修改下
            //因为时间列表只显示年月日的格式："yyyy-MM-dd"，所以转换一下dateTime1
            list.add(new DateTime(new DateTime().plusDays(i).toString("yyyy-MM-dd")).toDate());
        }

        int start = (pageNum - 1) * pageSize;
        int end = (start + pageSize) <= list.size() ? (start + pageSize) : list.size();
//        if (end > list.size()) {
//            end = list.size()
//        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> datePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize, list.size());
        datePage.setRecords(list.subList(start,end));
        return datePage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm） 和 字符串类型，转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospitalByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }
    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
