package com.prison.enums;

public enum SysLogModule {
    AUTH("用户认证"),
    PRISONER("服刑人员管理"),
    PRISONER_TRANSFER("服刑人员调动"),
    INCIDENT("事件管理"),
    VISITOR("访客管理"),
    PATROL("巡查管理"),
    PATROL_HANDOVER("巡查交接班"),
    MEDICAL("医疗记录"),
    USER("用户管理"),
    ROLE("角色管理"),
    GUARD("狱警管理"),
    PRISON_AREA("监区管理"),
    CELL("监舍管理"),
    FOLLOW_UP("重点关注"),
    SYSTEM("系统管理");

    private final String description;

    SysLogModule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
