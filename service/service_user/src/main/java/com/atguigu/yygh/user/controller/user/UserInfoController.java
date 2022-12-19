package com.atguigu.yygh.user.controller.user;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-12-09
 */
@RestController
@RequestMapping("/user/userinfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    //注意：mybatisPlus中的save只能做添加；mongodb中的save可做添加，也可以做修改
    @PutMapping("/update")
    //加 @RequestBody 注解，是让前端传一个json对象过来；如果不加就是传一个普通对象过来；这里使用普通对象接收form表单数据即可
//    public R update(@RequestHeader String token, @RequestBody UserAuthVo userAuthVo) {
    public R update(@RequestHeader String token, UserAuthVo userAuthVo) { //userAuthVo使用pojo类接收,userAuthVo是专门用于用户实名认证的Vo
        Long userId = JwtHelper.getUserId(token);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        userInfoService.updateById(userInfo);
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) { //加@RequestBody注解，让前端传一个json数据过来
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestHeader(value = "token") String token) {   //方式二
//    public R getUserInfo(HttpServletRequest request) {   //方式一
//        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);

        UserInfo userInfo = userInfoService.getUserInfo(userId);


        return R.ok().data("userInfo", userInfo);
    }
}

