package com.melody.concert;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.melody.concert", "com.melody.common"})
@EnableDiscoveryClient
@MapperScan("com.melody.concert.mapper")
public class ConcertApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConcertApplication.class, args);
        System.out.println("========== concert-service 启动成功 ==========");
    }
}