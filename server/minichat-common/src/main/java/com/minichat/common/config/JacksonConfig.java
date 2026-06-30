package com.minichat.common.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Jackson 序列化配置
 *
 * 把所有 Long 字段序列化为 String,避免前端 JS 收到 雪花 ID 时丢精度
 * (Number 安全整数上限 = 2^53-1,而 MyBatis-Plus 雪花 ID 远超该值)
 *
 * 只要服务引入 minichat-common,且启动类 scanBasePackages 覆盖到 com.minichat.*,即自动生效
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
