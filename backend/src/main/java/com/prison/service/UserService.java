package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.LoginRequest;
import com.prison.dto.LoginResponse;
import com.prison.dto.UserDTO;
import com.prison.entity.User;

public interface UserService extends IService<User> {
    LoginResponse login(LoginRequest request);
    Page<User> pageUsers(int page, int size, String keyword);
}