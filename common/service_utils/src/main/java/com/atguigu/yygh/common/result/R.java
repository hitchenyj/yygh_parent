package com.atguigu.yygh.common.result;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyj
 * @create 2022-11-26 9:34
 *
 R {
    code:20000,
    success:true,
    message:"说明信息",
    data: {
        "items":[{}, {}]
    }
 }

返回分页信息：
 R {
    code:20001,
    success:false,
    message:"说明信息",
    data: {
        "total":39,
        "rows":[{}, {}]
    }
 }


 *
 */
@Getter
@ToString
public class R {
    private Integer code;
    private Boolean success;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    //为了不允许外界创建R对象，先把它的构造器私有化
    // 构造器私有化之后，外界就创建不了了
    private R() {
    }

    // 提供一个静态方法，让它以类的方式调用
    public static R ok() {
        R r = new R(); //构造器私有化只是在这个类之外不能创建对象了；在这当前类里面还是可以的
        r.code = REnum.SUCCESS.getCode();
        r.success = REnum.SUCCESS.getFlag();
        r.message = REnum.SUCCESS.getMessage();
        return r;
    }

    public static R error() {
        R r = new R(); //构造器私有化只是在这个类之外不能创建对象了；在这当前类里面还是可以的
        r.code = REnum.ERROR.getCode();
        r.success = REnum.ERROR.getFlag();
        r.message = REnum.ERROR.getMessage();
        return r;
    }

    public R code(Integer code) {
        this.code = code;
        return this;
    }

    public R success(Boolean success) {
        this.success = success;
        return this;
    }

    public R message(String message) {
        this.message = message;
        return this;
    }

    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map) {
        this.data = map;
        return this;
    }
}
