package com.minichat.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)                          // 关闭跨站防护（API 不需要）
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态
                .authorizeHttpRequests(auth -> auth
                        // 内部 RPC 接口禁止从网关外部访问
                        .requestMatchers("/internal/**").denyAll()
                        // 认证接口公开
                        .requestMatchers("/auth/login", "/auth/register", "/auth/sms/code").permitAll()
                        // Knife4j / Swagger 静态资源
                        .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**", "/favicon.ico").permitAll()
                        // 网关已做 JWT 鉴权并注入 userId header，下游信任网关
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
