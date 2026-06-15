package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.SysLogQueryDTO;
import com.prison.entity.SysLog;
import com.prison.entity.User;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.enums.SysLogStatus;
import com.prison.mapper.SysLogMapper;
import com.prison.mapper.UserMapper;
import com.prison.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private final UserMapper userMapper;

    @Override
    public Page<SysLog> pageLogs(SysLogQueryDTO queryDTO) {
        int pageNum = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int pageSize = queryDTO.getSize() != null ? queryDTO.getSize() : 10;

        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(SysLog::getOperatorUsername, queryDTO.getKeyword())
                    .or()
                    .like(SysLog::getOperatorRealName, queryDTO.getKeyword())
                    .or()
                    .like(SysLog::getDetail, queryDTO.getKeyword())
                    .or()
                    .like(SysLog::getTargetName, queryDTO.getKeyword()));
        }

        if (StringUtils.hasText(queryDTO.getModule())) {
            wrapper.eq(SysLog::getModule, queryDTO.getModule());
        }

        if (StringUtils.hasText(queryDTO.getAction())) {
            wrapper.eq(SysLog::getAction, queryDTO.getAction());
        }

        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(SysLog::getStatus, queryDTO.getStatus());
        }

        if (StringUtils.hasText(queryDTO.getOperatorUsername())) {
            wrapper.eq(SysLog::getOperatorUsername, queryDTO.getOperatorUsername());
        }

        if (StringUtils.hasText(queryDTO.getTargetType())) {
            wrapper.eq(SysLog::getTargetType, queryDTO.getTargetType());
        }

        wrapper.orderByDesc(SysLog::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void log(SysLogModule module, SysLogAction action, String detail,
                    String targetType, Long targetId, String targetName, boolean success, String failReason) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setModule(module.name());
            sysLog.setAction(action.name());
            sysLog.setDetail(detail);
            sysLog.setTargetType(targetType);
            sysLog.setTargetId(targetId);
            sysLog.setTargetName(targetName);
            sysLog.setStatus(success ? SysLogStatus.SUCCESS.name() : SysLogStatus.FAILURE.name());
            sysLog.setFailReason(failReason);

            fillOperatorInfo(sysLog);
            fillRequestInfo(sysLog);

            save(sysLog);
        } catch (Exception e) {
            log.error("记录系统日志失败", e);
        }
    }

    @Override
    public void logSuccess(SysLogModule module, SysLogAction action, String detail,
                           String targetType, Long targetId, String targetName) {
        log(module, action, detail, targetType, targetId, targetName, true, null);
    }

    @Override
    public void logFailure(SysLogModule module, SysLogAction action, String detail,
                           String targetType, Long targetId, String targetName, String failReason) {
        log(module, action, detail, targetType, targetId, targetName, false, failReason);
    }

    private void fillOperatorInfo(SysLog sysLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                sysLog.setOperatorUsername(username);
                try {
                    User user = userMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                                    .eq(User::getUsername, username)
                    );
                    if (user != null) {
                        sysLog.setOperatorRealName(user.getRealName());
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void fillRequestInfo(SysLog sysLog) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setIpAddress(getClientIp(request));
                sysLog.setRequestMethod(request.getMethod());
                sysLog.setRequestUrl(request.getRequestURI());
            }
        } catch (Exception ignored) {
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
