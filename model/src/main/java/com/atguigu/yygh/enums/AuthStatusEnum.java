package com.atguigu.yygh.enums;

public enum AuthStatusEnum {

    NO_AUTH(0, "未认证"),
    AUTH_RUN(1, "认证中"),
    AUTH_SUCCESS(2, "认证成功"),
    AUTH_FAIL(-1, "认证失败"),
    ;

    private Integer status;
    private String name;

    AuthStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public static String getStatusNameByStatus(Integer status) {
        //先用AuthStatusEnum.values()拿到它的枚举项（就是一个枚举项数组），遍历这些枚举项
        AuthStatusEnum arrObj[] = AuthStatusEnum.values();
        for (AuthStatusEnum obj : arrObj) {
            //把传进来的status转换成int类型，和每个枚举项的status属性值做一个比较
            if (status.intValue() == obj.getStatus().intValue()) {
                return obj.getName();
            }
        }
        return ""; //遍历完枚举项没有发现相等的，就返回一个空串
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
