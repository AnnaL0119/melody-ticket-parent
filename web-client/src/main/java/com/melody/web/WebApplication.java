package com.melody.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        System.out.println("========== Gateway 启动成功 ==========");
        System.out.println("访问地址: http://localhost:8080");
    }
}
