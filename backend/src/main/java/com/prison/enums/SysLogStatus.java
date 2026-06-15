package com.prison.enums;

public enum SysLogStatus {
    SUCCESS("成功"),
    FAILURE("失败");

    private final String description;

    SysLogStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
