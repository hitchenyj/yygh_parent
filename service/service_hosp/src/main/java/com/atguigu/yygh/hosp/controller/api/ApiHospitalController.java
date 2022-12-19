package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-04 22:04
 */
@RestController //因为要返回json数据，所以用RestController
@RequestMapping("/api/hosp")
public class ApiHospitalController {

    @Autowired
    private HospitalService hospitalService;

//    @Autowired
//    private HospitalSetService hospitalSetService;

    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        //1. 获取所有的参数，并转化
        Map<String, Object> resultMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String requestSignKey = (String) resultMap.get("sign");
        String requestHoscode = (String) resultMap.get("hoscode");
        String platformSignKey = hospitalService.getSignKeyWithHoscode(requestHoscode);
        String encryptSignKey = MD5.encrypt(platformSignKey);

        //signKey的验证
        if (!StringUtils.isEmpty(requestSignKey) && !StringUtils.isEmpty(requestSignKey) && encryptSignKey.equals(requestSignKey)) {
            //上传数据中的logoData图片数据，是把一个图片经过base64加密之后，形成的一个密文，
            //base64的这些密文在传输过程中，会发生一些数据的变化：在传输过程中会把 "+" 转换为空格" "，
            // 所以传到platform之后，要把这些空格还原回去
            String logoData = (String) resultMap.get("logoData");
            String result = logoData.replaceAll(" ", "+");
            resultMap.put("logoData", result);
            //2. 调用service层保存医院信息
            hospitalService.saveHospital(resultMap);

            return Result.ok();
        } else {
            throw new YyghException(20001,"保存失败");
        }
    }

    @PostMapping("/hospital/show")
    public Result<Hospital> getHospitalInfo(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) stringObjectMap.get("hoscode");
        //这里也需要一个signKey的验证
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        return Result.ok(hospital);
    }
}
