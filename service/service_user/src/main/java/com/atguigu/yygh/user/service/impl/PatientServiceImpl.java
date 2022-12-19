package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-12-12
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAll(String token) {
        Long userId = JwtHelper.getUserId(token);
        QueryWrapper<Patient> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        List<Patient> patients = baseMapper.selectList(queryWrapper);
        patients.stream().forEach(item -> {
            this.packagePatient(item);
        });
        return patients;
    }

    @Override
    public Patient detail(Long id) {
        Patient patient = baseMapper.selectById(id);
        this.packagePatient(patient);
        return patient;
    }

    @Override
    public List<Patient> selectList(QueryWrapper<Patient> queryWrapper) {
        List<Patient> patients = baseMapper.selectList(queryWrapper);
        patients.forEach(item -> {
            this.packagePatient(item);
        });
        return patients;
    }

    public void packagePatient(Patient patient) {
        patient.getParam().put("certificatesTypeString", dictFeignClient.getNameByValue(Long.parseLong(patient.getCertificatesType())));
        String provinceAddr = dictFeignClient.getNameByValue(Long.parseLong(patient.getProvinceCode()));
        String cityAddr = dictFeignClient.getNameByValue(Long.parseLong(patient.getCityCode()));
        String districtAddr = dictFeignClient.getNameByValue(Long.parseLong(patient.getDistrictCode()));
        patient.getParam().put("provinceString", provinceAddr);
        patient.getParam().put("cityString", cityAddr);
        patient.getParam().put("districtString", districtAddr);
        String fullAddress = provinceAddr + cityAddr + districtAddr + patient.getAddress();
        patient.getParam().put("fullAddress", fullAddress);
    }
}
