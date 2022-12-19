package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-05 8:34
 */
public interface HospitalService {
    void saveHospital(Map<String, Object> resultMap);

    String getSignKeyWithHoscode(String requestHoscode);

    Hospital getHospitalByHoscode(String hoscode);

    Page<Hospital> getHospitalPage(Integer pageNume, Integer pageSize, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Hospital detail(String id);

    List<Hospital> findByHosnameLike(String name);

    Hospital getHospitalDetail(String hoscode);
}
