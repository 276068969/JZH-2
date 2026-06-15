package com.prison.enums;

public enum SysLogAction {
    LOGIN("登录"),
    LOGIN_FAIL("登录失败"),
    LOGOUT("登出"),
    CREATE("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    APPROVE("审批通过"),
    REJECT("审批驳回"),
    START_PROCESSING("开始处置"),
    RESOLVE("处置完成"),
    CLOSE("关闭"),
    START_VISIT("开始会见"),
    END_VISIT("结束会见"),
    CANCEL("取消"),
    TRANSFER("调动"),
    EXPORT("导出");

    private final String description;

    SysLogAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
