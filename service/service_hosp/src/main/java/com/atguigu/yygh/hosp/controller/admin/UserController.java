package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.acl.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-11-28 21:24
 */
@RestController
@RequestMapping(value = "/admin/user")
//@CrossOrigin //改由gateway统一做跨域处理
public class UserController {

    @PostMapping("/login")
    public R login(@RequestBody User user) {
        //暂时不去数据库中查：做用户系统时再去
        //在登陆这里，后台返回给前端一个token信息，token的值就是："admin-token"；
        //前端会对这个token处理：它把这个token值取出来，在前端做了一个保存，以后，每次它在访问后端微服务的时候，都会携带这个token；
        //只不过，它携带的key（键）不再是"token",而是“x-token”
        return R.ok().data("token", "admin-token");
    }

    @GetMapping("/info")
    public R info(String token) {
        //暂时不去数据库中查：做用户系统时再去
        List<String> list = new ArrayList<>();
        list.add("admin");
        Map<String, Object> map = new HashMap<>();
        map.put("roles", list);
        map.put("introduction", "I am a super administrator");
        map.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name", "Super Admin");

        return R.ok().data(map);
    }
}
