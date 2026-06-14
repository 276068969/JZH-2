package com.prison.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatrolHandoverConfirmDTO {

    @NotNull(message = "接班警员不能为空")
    private Long incomingGuardId;

    private String incomingGuardName;
}
