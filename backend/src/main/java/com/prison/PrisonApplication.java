package com.prison;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.prison.mapper")
public class PrisonApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrisonApplication.class, args);
    }
}