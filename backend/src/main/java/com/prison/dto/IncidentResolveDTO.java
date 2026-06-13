package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IncidentResolveDTO {
    @NotBlank(message = "处置结论不能为空")
    private String handlerResult;
}
