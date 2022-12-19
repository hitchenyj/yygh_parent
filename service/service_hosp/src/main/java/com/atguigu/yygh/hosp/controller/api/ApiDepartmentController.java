package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-05 14:03
 */
@RestController
@RequestMapping("/api/hosp")
public class ApiDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证：signKey(省略)
        departmentService.saveDepartment(stringObjectMap);
        return Result.ok();
    }

    @PostMapping("/department/list")
    public Result<Page> getDepartmentPage(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证：signKey(省略)
        Page<Department> page = departmentService.getDepartmentPage(stringObjectMap);

        return Result.ok(page);
    }

    @PostMapping("/department/remove")
    public Result remove(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //验证：signKey(省略)
        departmentService.remove(stringObjectMap);
        return Result.ok();
    }

}
