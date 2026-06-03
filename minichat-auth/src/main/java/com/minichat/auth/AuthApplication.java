package com.minichat.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient      // Auth 服务启动时自动注册到 Nacos 注册中心
public class AuthApplication {
    public static void main(String[] args) {
        // 运行这个类:
        // 1. 启动一个内置的 Tomcat 服务器
        // 2. 创建 Spring 容器
        // 3. 扫描并加载所有的 Bean
        // 4. 作为 Spring Boot 应用程序的入口点，启动后开始工作
        SpringApplication.run(AuthApplication.class, args);
    }
}
