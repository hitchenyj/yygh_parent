package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.repository.HospRespository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author chenyj
 * @create 2022-12-05 8:34
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospRespository hospRespository;

    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void saveHospital(Map<String, Object> resultMap) {
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(resultMap), Hospital.class);

        String hoscode = hospital.getHoscode();
        Hospital hosp = hospRespository.findByHoscode(hoscode);

        if (hosp == null) { //平台上原来没有医院信息，做添加操作
            //在做添加的时候，一定也要设置几个Hospital中有，而没有传过来的其它几个字段
            hospital.setStatus(0); //0:未上线， 1: 已上线
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0); //1:已删除，0:未删除
            hospRespository.save(hospital);
        } else { //平台上有医院信息，做修改操作
            hospital.setStatus(hosp.getStatus()); //0:未上线， 1: 已上线
            hospital.setCreateTime(hosp.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(hosp.getIsDeleted()); //1:已删除，0:未删除

            hospital.setId(hosp.getId()); //修改是根据id做修改的，所以要把id从查出来的记录里取出再传进去
            hospRespository.save(hospital);
        }
    }

    @Override
    public String getSignKeyWithHoscode(String requestHoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", requestHoscode);
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(queryWrapper);
        if (hospitalSet == null) {
            throw new YyghException(20001,"该医院不存在！");
        }
        return hospitalSet.getSignKey();
    }

    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        return hospRespository.findByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNume, Integer pageSize, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
//        if (!StringUtils.isEmpty(hospitalQueryVo.getHosname()))
//        {
//            hospital.setHosname(hospitalQueryVo.getHosname());
//        }
//        if (!StringUtils.isEmpty(hospitalQueryVo.getProvinceCode()))
//        {
//            hospital.setProvinceCode(hospitalQueryVo.getProvinceCode());
//        }
//        if (!StringUtils.isEmpty(hospitalQueryVo.getCityCode()))
//        {
//            hospital.setCityCode(hospitalQueryVo.getCityCode());
//        }
        BeanUtils.copyProperties(hospitalQueryVo, hospital);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
//                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //所有字段都支持模糊查询；
                .withMatcher("hosname", ExampleMatcher.GenericPropertyMatchers.contains())  //name字段模糊匹配
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        Example<Hospital> example = Example.of(hospital, matcher);

        Pageable pageable = PageRequest.of(pageNume-1, pageSize, Sort.by("createTime").ascending());
        Page<Hospital> page = hospRespository.findAll(example, pageable);

//        for (Hospital hospital1 : page) {}
        //这里使用流的方式代替for循环
        page.getContent().stream().forEach(item -> {
            this.packageHospital(item);
        });

        return page;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if (status == 0 || status == 1) {
            Hospital hospital = hospRespository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospRespository.save(hospital);
        }
    }

    @Override
    public Hospital detail(String id) {
        Hospital hospital = hospRespository.findById(id).get();
        this.packageHospital(hospital);
        return hospital;
    }

    @Override
    public List<Hospital> findByHosnameLike(String name) {
        //自定义方法: findBy + pojo对象的属性名(属性名首字母大写),只要按照规则把方法名在接口中定义出了就可以了
        return hospRespository.findByHosnameLike(name);
    }

    @Override
    public Hospital getHospitalDetail(String hoscode) {
        Hospital hospital = hospRespository.findByHoscode(hoscode);
        this.packageHospital(hospital);
        return hospital;
    }

    private void packageHospital(Hospital hospital) {

        String hostype = hospital.getHostype();
        String provinceCode = hospital.getProvinceCode();
        String cityCode = hospital.getCityCode();
        String districtCode = hospital.getDistrictCode();

        //拿到这些编码信息之后，理论上应该去dict表中查询；但是不可以！因为是微服务，尽量要求一个微服务操作一个数据库
        //需要让service_cmn提供接口，然后在service_hosp里远程调用service_cmn；然后，在service_cmn里再让它查询它自己对应的库中的dict表
        //然后用OpenFeign进行远程调用
        String provinceAddress = dictFeignClient.getNameByValue(Long.parseLong(provinceCode));
        String cityAddress = dictFeignClient.getNameByValue(Long.parseLong(cityCode));
        String districtAddress = dictFeignClient.getNameByValue(Long.parseLong(districtCode));

        String level = dictFeignClient.getNameByDictCodeAndValue(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hostype));

        hospital.getParam().put("hostypeString", level);
        hospital.getParam().put("fullAddress", provinceAddress + cityAddress + districtAddress + hospital.getAddress());
    }
}
