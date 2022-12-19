package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-09 8:22
 */
@RestController
@RequestMapping("/user/hosp/hospital")
public class UserHospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping("/list")
    public R getHospitalList(HospitalQueryVo hospitalQueryVo) { //因为是GetMapping，这里就不能再加@RequestBody注解了
        //调service层的分页方法: 管理员系统的Controller也调了HospitalService，现在user系统和admin共用同一个service
        Page<Hospital> page = hospitalService.getHospitalPage(1, 10000, hospitalQueryVo);
        return R.ok().data("list", page.getContent());
    }

    //注意：方法可以服用，但是Controller层和前端交互的接口不建议服用
    @GetMapping("/{name}")
    public R findByName(@PathVariable String name) {
        List<Hospital> list = hospitalService.findByHosnameLike(name);
        return R.ok().data("list", list);
    }

    @GetMapping("/detail/{hoscode}")
    public R getHospitalDetail(@PathVariable String hoscode) {
        Hospital hospital = hospitalService.getHospitalDetail(hoscode);
        return R.ok().data("hospital", hospital);
    }
}
