package com.prison.vo;

import lombok.Data;

@Data
public class FollowUpStatsVO {

    private Long todayPending;
    private Long weekPending;
    private Long monthPending;
    private Long overdue;

    private Long consecutiveMissed;
    private Long stillTreating;
    private Long keyAttention;

    private Long completed;
    private Long totalWithFollowUp;
}
