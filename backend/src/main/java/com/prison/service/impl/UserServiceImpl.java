package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.LoginRequest;
import com.prison.dto.LoginResponse;
import com.prison.dto.UserDTO;
import com.prison.entity.SysLog;
import com.prison.entity.User;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.enums.SysLogStatus;
import com.prison.mapper.SysLogMapper;
import com.prison.mapper.UserMapper;
import com.prison.security.JwtTokenProvider;
import com.prison.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SysLogMapper sysLogMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            saveLoginLog(request.getUsername(), null, false, "用户名不存在");
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            saveLoginLog(user.getUsername(), user.getRealName(), false, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getEnabled()) {
            saveLoginLog(user.getUsername(), user.getRealName(), false, "账号已被禁用");
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        saveLoginLog(user.getUsername(), user.getRealName(), true, null);

        return LoginResponse.builder()
                .id(user.getId())
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .roles(Collections.singletonList(user.getRole()))
                .build();
    }

    private void saveLoginLog(String username, String realName, boolean success, String failReason) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setModule(SysLogModule.AUTH.name());
            sysLog.setAction(success ? SysLogAction.LOGIN.name() : SysLogAction.LOGIN_FAIL.name());
            sysLog.setDetail(success ? "用户 " + username + " 登录系统" : "用户 " + username + " 登录失败：" + failReason);
            sysLog.setTargetType("USER");
            sysLog.setOperatorUsername(username);
            sysLog.setOperatorRealName(realName);
            sysLog.setStatus(success ? SysLogStatus.SUCCESS.name() : SysLogStatus.FAILURE.name());
            sysLog.setFailReason(failReason);

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

            sysLogMapper.insert(sysLog);
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

    @Override
    public Page<User> pageUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}