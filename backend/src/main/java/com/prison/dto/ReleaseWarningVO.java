package com.prison.dto;

import com.prison.entity.Prisoner;
import lombok.Data;

import java.util.List;

@Data
public class ReleaseWarningVO {
    private Integer days;
    private String label;
    private Integer count;
    private List<Prisoner> prisoners;

    public ReleaseWarningVO(Integer days, String label, Integer count, List<Prisoner> prisoners) {
        this.days = days;
        this.label = label;
        this.count = count;
        this.prisoners = prisoners;
    }
}
