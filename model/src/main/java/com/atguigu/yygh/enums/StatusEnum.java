package com.atguigu.yygh.enums;

/**
 * @author chenyj
 * @create 2022-12-13 13:03
 */
public enum StatusEnum {
    //枚举类里面首先应该是枚举项
    LOCK(0, "锁定"),
    NORMAL(1, "正常");

    private Integer status;
    private String statusDesc;

    public static String getDescByStatus(Integer status) {
        StatusEnum[] values = StatusEnum.values();
        for (StatusEnum value : values) {
            if (value.getStatus().intValue() == status.intValue()) {
                return value.getStatusDesc();
            }
        }

        return "";
    }

    StatusEnum(Integer status, String statusDesc) {
        this.status = status;
        this.statusDesc = statusDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    @Override
    public String toString() {
        return "StatusEnum{" +
                "status=" + status +
                ", statusDesc='" + statusDesc + '\'' +
                '}';
    }
}
