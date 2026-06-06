package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.campus.mapper")
@SpringBootApplication(scanBasePackages = "com.campus")
public class SwapCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwapCampusApplication.class, args);
    }
}
