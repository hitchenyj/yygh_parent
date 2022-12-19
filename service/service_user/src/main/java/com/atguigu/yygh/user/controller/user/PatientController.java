package com.atguigu.yygh.user.controller.user;


import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-12-12
 */
@RestController
@RequestMapping("/user/userinfo/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    //增
    @PostMapping("/save")
    public R save(@RequestBody Patient patient, @RequestHeader String token) {
        patient.setUserId(JwtHelper.getUserId(token));
        patientService.save(patient);
        return R.ok();
    }

    //删
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {

        patientService.removeById(id);
        return R.ok();
    }

    //改: 分两步：
    // 修改1. 先去修改页面回显数据；
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok().data("patient", patientService.detail(id));
    }
    //修改2.在回显页面真正地修改
    @PutMapping("/update")
//    public R update(Patient patient) {//前端传数据时是pojo对象，前端需要使用params参数
    public R update(@RequestBody Patient patient) {//前端传数据时是json数据格式传数据，前端需要使用data参数往后传
        patientService.updateById(patient);
        return R.ok();
    }

    //查:显示当前登录用户底下添加的所有就诊人信息
    @GetMapping("/all")
    public R findAll(@RequestHeader String token) {

        List<Patient> list = patientService.findAll(token);

        return R.ok().data("list", list);
    }

    //根据就诊人id获取patient
    @GetMapping("/{patientId}")
    public Patient getPatientById(@PathVariable("patientId") Long patientId) {
        return patientService.getById(patientId);
    }
}

