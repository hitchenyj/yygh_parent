package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRespository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenyj
 * @create 2022-12-05 14:07
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRespository departmentRespository;
    @Override
    public void saveDepartment(Map<String, Object> stringObjectMap) {
        //先把stringObjectMap对象转换为Department对象
        Department department = JSONObject.parseObject(JSONObject.toJSONString(stringObjectMap), Department.class);
        //根据 医院编号 + 科室编号 做联合查询
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();

        Department platformDepartment = departmentRespository.findByHoscodeAndDepcode(hoscode, depcode);
        if (platformDepartment == null) { //如果mongodb中没有该科室信息，做添加操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0); //1:已删除，0:未删除
            //在使用departmentRespository保存时可以使用insert或者save，因为上传科室信息，以后有时可能会做修改，所以，最好使用save
            departmentRespository.save(department);
        } else { // 如果mongodb中没有该科室信息，做修改操作
            department.setCreateTime(platformDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(platformDepartment.getIsDeleted()); //1:已删除，0:未删除
            department.setId(platformDepartment.getId());
            departmentRespository.save(department);
        }
    }

    @Override
    public Page<Department> getDepartmentPage(Map<String, Object> stringObjectMap) {
        Integer pageNum = Integer.parseInt((String) stringObjectMap.get("page"));
        Integer limit = Integer.parseInt((String) stringObjectMap.get("limit"));
        String hoscode = (String) stringObjectMap.get("hoscode");

        //带查询条件的分页查询
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        Pageable pageable = PageRequest.of(pageNum - 1, limit);
        Page<Department> all = departmentRespository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(Map<String, Object> stringObjectMap) {
        String hoscode = (String) stringObjectMap.get("hoscode");
        String depcode = (String) stringObjectMap.get("depcode");

        //首先根据医院编号和科室编号去mongodb里查询一下，获得id；再根据id删除
        Department department = departmentRespository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            departmentRespository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> getDepartmentList(String hoscode) {
        //1. 查询同一家医院下的所有科室
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> all = departmentRespository.findAll(example);

        //2. 对所有的部门进行分组
          //2.1先把List转换成流[使用流的方式是最简单的]
        //Map的Key:就是当前科室所属大科室的编号
        //Map的Value:就是当前同属于同一个大科室底下的所有子科室信息
        Map<String, List<Department>> collect = all.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //返回给前端的是DepartmentVo对象
        List<DepartmentVo> bigDepartmentList = new ArrayList<>();
        //遍历：先掉collect的entrySet，然后对这个entrySet使用for循环
        for (Map.Entry<String, List<Department>> entry : collect.entrySet()) {
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            //1. key：就是大科室的编号
            String bigcode = entry.getKey();
            //2. value：当前大科室底下子科室列表
            List<Department> departmentList = entry.getValue();

            bigDepartmentVo.setDepcode(bigcode);
            bigDepartmentVo.setDepname(departmentList.get(0).getBigname());

            List<DepartmentVo> childVoList = new ArrayList<>();
            //3.拿到大科室，并加入list之后，还没完，还要拿到大科室下所有的子科室
            for (Department child : departmentList) {
                DepartmentVo childVo = new DepartmentVo();
                //获取当前子科室的编号
                String depcode = child.getDepcode();
                //获取当前子科室的名字
                String depname = child.getDepname();
                childVo.setDepcode(depcode);
                childVo.setDepname(depname);
                childVoList.add(childVo);
            }
            bigDepartmentVo.setChildren(childVoList);

            bigDepartmentList.add(bigDepartmentVo);
        }
        return bigDepartmentList;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {

        Department department = departmentRespository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            return department.getDepname();
        }

        return "";
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRespository.findByHoscodeAndDepcode(hoscode, depcode);
    }
}
