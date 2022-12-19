package com.atguigu.yygh.common.exception;

/**
 * @author chenyj
 * @create 2022-11-26 18:44
 *
 * 这种自定义的Exception，如果想使用的话，必须是自己手动抛出
 */
public class YyghException extends RuntimeException{

    private Integer code;
    private String message;

    public YyghException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
