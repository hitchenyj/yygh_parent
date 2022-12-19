package com.atguigu.yygh.oss.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.oss.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author chenyj
 * @create 2022-12-11 23:14
 */
@RestController
@RequestMapping("/user/oss/file")
public class OssController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    public R upload(MultipartFile file) {
        //为了能让前端看到上传的图片，返回给前端一个上传后图片在阿里云的地址
        String url = ossService.upload(file);
        return R.ok().data("url", url);
    }
}
