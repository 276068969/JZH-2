package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VisitorApprovalDTO {
    @NotBlank(message = "审批意见不能为空")
    private String approveRemark;
    
    private Long approveGuardId;
}
