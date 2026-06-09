package com.prison.config;

import com.prison.entity.User;
import com.prison.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    private void initAdminUser() {
        List<User> users = userMapper.selectList(null);
        boolean needsUpdate = false;

        for (User user : users) {
            if (!user.getPassword().startsWith("$2a$")) {
                String rawPassword = switch (user.getUsername()) {
                    case "admin" -> "admin123";
                    case "manager" -> "manager123";
                    case "guard" -> "guard123";
                    case "doctor" -> "doctor123";
                    case "viewer" -> "viewer123";
                    default -> null;
                };
                if (rawPassword != null) {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    userMapper.updateById(user);
                    needsUpdate = true;
                    log.info("用户 {} 密码已加密", user.getUsername());
                }
            }
        }

        if (needsUpdate) {
            log.info("用户密码初始化完成");
        }
    }
}