package com.minichat.message.config;

import com.aliyun.sdk.service.oss2.OSSAsyncClient;
import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OssConfig {
//    @Value("${aliyun.oss}")
    private final OssProperties ossProperties;

    public OssConfig(OssProperties ossProperties){
        this.ossProperties = ossProperties;
    }

    @Bean(destroyMethod = "close")
    public OSSClient ossClient() {
        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        StaticCredentialsProvider staticCredentialsProvider = new StaticCredentialsProvider(ossProperties.getAccessKeyId(),ossProperties.getAccessKeySecret());
        OSSClientBuilder builder = OSSClient.newBuilder()
                .region(ossProperties.getRegion())
                .endpoint(ossProperties.getEndpoint())
                .credentialsProvider(staticCredentialsProvider);
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    public OSSAsyncClient ossAsyncClient() {
        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        return OSSAsyncClient.newBuilder()
                .region(ossProperties.getRegion())
                .endpoint(ossProperties.getEndpoint())
                .credentialsProvider(provider)
                .build();
    }
}