package com.atguigu.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.excel.Student;
import com.atguigu.yygh.cmn.excel.StudentReadListener;
import com.atguigu.yygh.cmn.listener.DictEevoListener;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-11-30
 */
@RestController
@RequestMapping("/admin/cmn")
//@CrossOrigin //改由gateway统一做跨域处理
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/childList/{pid}")
    public R getChildListByParentId(@PathVariable Long pid) {
        List<Dict> list =  dictService.getChildListByPid(pid);
        return R.ok().data("items", list);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {

        dictService.download(response);

//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//        EasyExcel.write(response.getOutputStream(), DownloadData.class).sheet("模板").doWrite(data());
    }

    @PostMapping("/upload")
    public R upload(HttpServletResponse response, MultipartFile file) throws IOException { // 注意，这个参数名是写死的，必须叫：file，因为在SSM中，这个参数名必须跟前端表单form的，上传组件中的name属性值一致，现在前端name值默认是：file
        // 在后端接收了这个文件之后，后端要对这个excel文件做解析：就是excel文件的读取
        // 这里解析必须使用输入流的方式
        //这里解析必须使用DictEeVo，不能用Dict，因为Dict上没有 @ExcelProperty 注解
        dictService.upload(file);
//        response.setHeader();
        return R.ok();
    }

    //根据医院所属的省市区编号，获取省市区文字
    //注意，上面写的接口都是给前端使用的，所以，返回的是R对象，现在是从hosp微服务调cmn微服务，不是给前端使用的，所以，hosp微服务需要什么样的返回值，cmn就之间返回什么样的返回值，不需要使用R对象
    @GetMapping("/{value}") //查询操作，使用 @GetMapping即可
    //需要注意的是：以前写@PathVariable注解，如果是前端调用它的话，直接把这个占位符赋值给参数变量了；
    //       但是，现在这个接口不是给前端调用，而是给另一个微服务调用，给另一个微服务调用时，@PathVariable必须要指定value属性值，value属性值要和占位符中的保持一致,省略容易出问题
    public String getNameByValue(@PathVariable("value") Long value) {  //value类型需要跟数据库中的类型保持一致
        return dictService.getNameByValue(value);
    }

    //根据医院的等级编号，获取医院等级信息
    @GetMapping("/{dictCode}/{value}")
    public String getNameByDictCodeAndValue(@PathVariable("dictCode") String dictCode,
                                            @PathVariable("value") Long value) {  //value类型需要跟数据库中的类型保持一致
        return dictService.getNameByDictCodeAndValue(dictCode, value);
    }
}

