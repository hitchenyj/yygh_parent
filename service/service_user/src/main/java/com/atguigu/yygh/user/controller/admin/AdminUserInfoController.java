package com.atguigu.yygh.user.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-13 10:02
 */
@RestController
@RequestMapping("/administrator/userinfo/")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/{pageNum}/{limit}")
    public R getUserInfoPage(@PathVariable Integer pageNum,
                             @PathVariable Integer limit,
                              UserInfoQueryVo userInfoQueryVo) { //由于前面使用的是GetMapping，这里就不要加@RequestBody了，否则，就要换成PostMapping

        Page<UserInfo> page = userInfoService.getUserInfoPage(pageNum, limit, userInfoQueryVo);
        //返回给前端，前端做分页只需要总记录数:total; 以及当前页列表数据list
        return R.ok().data("total", page.getTotal()).data("list", page.getRecords());
    }

    @PutMapping("/update/{id}/{status}")
    public R update(@PathVariable Long id, @PathVariable Integer status) {
        userInfoService.updateStatus(id, status);
        return R.ok();
    }

    @PutMapping("/auth/{id}/{authStatus}")
    public R approval(@PathVariable Long id, @PathVariable Integer authStatus) {
        if (authStatus == 2 || authStatus == -1) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAuthStatus(authStatus);

            userInfoService.updateById(userInfo);
        }
        return R.ok();
    }

    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Integer id) {
        Map<String, Object> map = userInfoService.detail(id);
        return R.ok().data(map);
    }

}
