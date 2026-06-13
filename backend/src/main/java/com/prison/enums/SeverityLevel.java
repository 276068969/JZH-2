package com.prison.enums;

import lombok.Getter;

@Getter
public enum SeverityLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("严重");

    private final String description;

    SeverityLevel(String description) {
        this.description = description;
    }

    public boolean isHighOrAbove() {
        return this == HIGH || this == CRITICAL;
    }

    public static SeverityLevel fromString(String severity) {
        if (severity == null) {
            return null;
        }
        try {
            return valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
