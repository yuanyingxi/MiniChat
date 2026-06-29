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
                        .requestMatchers("/auth/**", "/user/**", "/friend/**", "/group/**", "/file/**", "/internal/**").permitAll() // 目前没有网关，
                        // Knife4j / Swagger 静态资源
                        .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()                       // 其他接口要认证
                );
        return http.build();
    }
}
