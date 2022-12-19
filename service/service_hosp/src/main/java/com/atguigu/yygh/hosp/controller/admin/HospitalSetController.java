package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/*
    swagger的几个注解：
    1. @Api(tags = ""): 标记在controller层的接口类上的，
    2. @ApiOperation(value = ""): 标记在controller中方法上的，
    3. @ApiParam(value = ""): 标记在controller中方法上的参数上的

    4. @ApiModel(description = ""): 标记在POJO类上的，对POJO类做说明的
    5. @ApiModelProperty(value = ""): 标记在POJO类的属性上的，对POJO类的属性做说明
 */

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-11-25
 */
@RestController
@Api(tags = "医院设置信息")
@RequestMapping("/admin/hosp/hospitalSet")
@Slf4j
//@CrossOrigin //改由gateway统一做跨域处理
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    /*
        做带条件查询的分页
        返回R对象
        Rest URL中携带当前页号，以及每页显示多少条
     */
    @ApiOperation(value = "带查询条件的分页查询")
    @PostMapping(value = "/page/{pageNum}/{size}")
    public R getpageInfo(@ApiParam(name = "pageNum", value = "当前页号", required = true) @PathVariable Integer pageNum,
                         @ApiParam(name = "size", value = "每页显示多少条", required = true) @PathVariable Integer size,
                         @RequestBody HospitalSetQueryVo hospitalSetQueryVo) {//如果前端给后端发送的是Json数据，这里必须使用 @RequestBody注解

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        Page<HospitalSet> page = new Page<>(pageNum, size);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty( hospitalSetQueryVo.getHosname())) {
            queryWrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }

        if (!StringUtils.isEmpty( hospitalSetQueryVo.getHoscode())) {
            queryWrapper.eq("hoscode", hospitalSetQueryVo.getHoscode());
        }

        //调用service层做分页查询的接口
        hospitalSetService.page(page, queryWrapper);

        return R.ok().data("total", page.getTotal()).data("rows", page.getRecords());
    }

    /*
        新增
     */
    @ApiOperation(value = "新增一条记录接口")
    @PostMapping("/save")
    public R save(@RequestBody HospitalSet hospitalSet) {
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);

        Random random = new Random();
        //签名SignKey：取系统当前时间戳 + 随机数，然后再用MD5加密
        //random.nextInt(1000): 参数可以生成多大范围之内的随机数
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        hospitalSetService.save(hospitalSet);

        return R.ok();
    }

    /*
        修改之回显数据
     */
//    @ApiOperation(value = "")
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Integer id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("item", hospitalSet);
    }

    /*
    修改之修改数据
    */
    @PutMapping("/update")
    public R update(@RequestBody HospitalSet hospitalSet) {
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    /*
        批量删除
     */
    @DeleteMapping("/delete")
    public R batchDelete(@RequestBody List<Integer> ids) {
        hospitalSetService.removeByIds(ids);
        return R.ok();
    }

    //根据医院设置id删除医院设置信息
    @ApiOperation(value = "根据医院设置id删除医院设置信息")
    @DeleteMapping(value = "/deleteById/{id}")
    public R deleteById(@PathVariable Integer id) {
        hospitalSetService.removeById(id);
        return R.ok();
    }
    /*
        锁定与解锁
     */
    @PutMapping("/status/{id}/{status}")
    public R updateStatus(@PathVariable Long id, @PathVariable Integer status) {

        log.info("current thread is:" + Thread.currentThread().getId() + ",param: id = " + id);
        //way 1: 适合于高并发，使用乐观锁的情况
//        HospitalSet hospitalSet = hospitalSetService.getById(id);
//        hospitalSet.setStatus(status);
//        hospitalSetService.updateById(hospitalSet);

        //way 2：传哪些参数就修改哪些参数，不传就不修改
        HospitalSet hospitalSet = new HospitalSet();
        hospitalSet.setId(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);

        log.info("result: " + Thread.currentThread().getId() + hospitalSet.toString());

        return R.ok();
    }

    /*
        开发中建议使用Restful风格的请求方式：就是使用不同的请求方式表示对数据库的资源进行何种处理
        GET : 表示查询数据库中的数据
        POST : 表示添加数据
        PUT : 表示修改数据
        DELETE: 表示删除数据
        如果需要传递id值的话，建议在请求路径URL中传递，比如：/XX/id，不再建议使用参数（/XX?id=3）的方式传递
     */
    @ApiOperation(value = "查询所有的医院设置信息")
    @GetMapping(value = "/findAll")
    public R findAll() {
//        int result = 10 / 0;

        List<HospitalSet> list = hospitalSetService.list();
        if (list.size() == 0) {
            log.error("ERROR: HospitalSet表为空异常");
            throw new YyghException(20013, "HospitalSet表为空异常");
        }
        return R.ok().data("items", list);
    }

}

