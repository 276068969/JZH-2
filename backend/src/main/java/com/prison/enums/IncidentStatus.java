package com.prison.enums;

import lombok.Getter;

@Getter
public enum IncidentStatus {
    PENDING("待处理", 0),
    PROCESSING("处理中", 1),
    RESOLVED("已解决", 2),
    CLOSED("已关闭", 3);

    private final String description;
    private final int order;

    IncidentStatus(String description, int order) {
        this.description = description;
        this.order = order;
    }

    public boolean canTransitionTo(IncidentStatus targetStatus) {
        if (this == CLOSED) {
            return false;
        }
        return targetStatus.order == this.order + 1;
    }

    public static IncidentStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
