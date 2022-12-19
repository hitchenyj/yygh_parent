package com.atguigu.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.prop.WeixinProperties;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-12-10 21:48
 */
@Controller
@RequestMapping("/user/userinfo/wx")
public class WeixinController {

//    @Value("${weixin.app_id}") //获取配置文件application.properties中属性值的方式1
//    private String appId;

    @Autowired //注入WeixinProperties配置类对象
    private WeixinProperties weixinProperties;

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/param")
    @ResponseBody
    public R getWeixinLoginParam() throws UnsupportedEncodingException {
        //主要是把url中的特殊字符(如：/, : 等)做一个编码特殊处理，这样在返回给微信二维码时不容易出问题
        String redirecturl = URLEncoder.encode(weixinProperties.getRedirecturl(), "utf-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", weixinProperties.getAppid());
        map.put("scope", weixinProperties.getScope());
        map.put("redirecturl", redirecturl);
        map.put("state", System.currentTimeMillis() + "");
        return R.ok().data(map);
    }


    @GetMapping("/callback")
//    public String callback(@RequestParam("code") String xxx, @RequestParam("state") String yyy) {
    public String callback(String code, String state) throws Exception {

        //StringBuilder: 里面的方法都没有加锁，效率高！
        //StringBuffer: 里面的方法都是加了同步锁（synchronized）的，效率低，但是是线程安全的
        //这里不存在共享数据，所以，不存在线程安全问题，所以就使用高效的StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder append = stringBuilder.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(append.toString(), weixinProperties.getAppid(), weixinProperties.getAppsecret(), code);

        String result = HttpClientUtils.get(format);

        JSONObject jsonObject = JSONObject.parseObject(result);
        //access_token是第三方应用访问微信服务器的一个凭证（接口调用凭证）
        String access_token = jsonObject.getString("access_token");
        //openid是扫描确认的这个用户在微信服务器上的唯一标识符
        String openid = jsonObject.getString("openid");
        //拿到这个openid之后，就直接去本地库的user_info表中查一下，本地库里有没有这个用户信息（这个用户以前是否登录过）
        //如果没有，说明这是他首次用微信登录；如果有，说明他以前登录过
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("openid", openid);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if (userInfo == null) { //首次使用微信登录，把用户的微信信息在表中保存一下
            userInfo = new UserInfo();
            userInfo.setOpenid(openid);
            //给微信服务器发请求，获取当前扫码用户的微信信息
            StringBuilder sb = new StringBuilder();
            StringBuilder append1 = sb.append("https://api.weixin.qq.com/sns/userinfo")
                    .append("?access_token=%s")
                    .append("&openid=%s");

            //再次请求微信服务器，获取用户信息（昵称、头像等）
            String s = HttpClientUtils.get(String.format(append1.toString(), access_token, openid));
            //也可以使用GSON解析
            JSONObject jsonObject1 = JSONObject.parseObject(s);
            String nickname = jsonObject1.getString("nickname");
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }
        //不管是否是首次登录都要获取用户信息，所以，暂时先去掉else
        //else {} //说明以前用微信登录过

        //5. 验证用户的status
        if (userInfo.getStatus() == 0) { //状态（0：锁定 1：正常）
            throw new YyghException(20001, "用户锁定中");
        }
        //6. 返回用户信息
        Map<String, String> map = new HashMap<>();

        //检查这个用户手机号是否为空，说明：① 以前微信还没有绑定过手机号; ② 现在肯定是首次使用微信登录
        //如果是首次微信登录，要强制绑定手机号
        if (StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", openid);//没做过手机号绑定，返回给前端的openid不为空
        } else {//这个用户手机号是不为空，说明这不是首次使用微信登录
            map.put("openid", ""); //已做过手机号绑定，返回给前端的openid为空
        }

        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        //注意：不能直接使用userInfo.getName()，因为这时不一定有用户名，使用name最保险
//        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);

        //跳转到前端页面：这是一个固定路由的跳转页面，只要提到路由对应的页面就都在pages目录下，所以，它会去用户系统的->pages->weixin目录下找 callback.vue 页面
        return "redirect:http://localhost:3000/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode(map.get("name"),"utf-8");
    }
}
