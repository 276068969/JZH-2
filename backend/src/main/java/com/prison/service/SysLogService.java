package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.SysLogQueryDTO;
import com.prison.entity.SysLog;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;

public interface SysLogService extends IService<SysLog> {

    Page<SysLog> pageLogs(SysLogQueryDTO queryDTO);

    void log(SysLogModule module, SysLogAction action, String detail,
             String targetType, Long targetId, String targetName, boolean success, String failReason);

    void logSuccess(SysLogModule module, SysLogAction action, String detail,
                    String targetType, Long targetId, String targetName);

    void logFailure(SysLogModule module, SysLogAction action, String detail,
                    String targetType, Long targetId, String targetName, String failReason);
}
