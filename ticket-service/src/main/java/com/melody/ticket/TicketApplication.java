package com.melody.ticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.melody.ticket", "com.melody.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.melody.ticket.mapper")
public class TicketApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
        System.out.println("========== ticket-service 启动成功 ==========");
    }
}