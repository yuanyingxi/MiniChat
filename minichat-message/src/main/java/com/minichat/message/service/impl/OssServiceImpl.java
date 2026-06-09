package com.minichat.message.service.impl;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.models.*;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.minichat.message.config.OssProperties;
import com.minichat.message.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSSClient ossClient;
    private final OssProperties ossProperties;

    @Override
    public String uploadFile(MultipartFile file, String objectKey) {
        try {
            String contentType = file.getContentType();
            return uploadFile(file.getInputStream(), objectKey, contentType);
        } catch (IOException e) {
            log.error("读取上传文件流失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectKey, String contentType) {
        try {
            // 构建 PutObject 请求，支持设置元数据、存储类型、ACL 等
            PutObjectRequest request = PutObjectRequest.newBuilder()
                    .bucket(ossProperties.getBucketName())
                    .key(objectKey)
                    .body(BinaryData.fromStream(inputStream))
                    .contentType(contentType)
                    .storageClass("Standard")  // 标准存储
                    .build();
            // 执行上传
            PutObjectResult response = ossClient.putObject(request);

            log.info("文件上传成功 - bucket: {}, key: {}, Etag: {}",
                    ossProperties.getBucketName(), objectKey, response.eTag());

            return generatePresignedUrl(objectKey, 3600);  // 默认 1 小时有效

        } catch (Exception e) {
            log.error("文件上传到 OSS 失败: objectKey={}", objectKey, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.newBuilder()
                    .bucket(ossProperties.getBucketName())
                    .key(objectKey)
                    .build();

            GetObjectResult response = ossClient.getObject(request);

            // 注意：GetObjectResponse 实现了 AutoCloseable，应在 try-with-resources 中使用
            // 小对象 (≤32KB) 会加载到内存，大对象直接从网络流式读取
            return response.body();

        } catch (Exception e) {
            log.error("从 OSS 下载文件失败: objectKey={}", objectKey, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public boolean deleteFile(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.newBuilder()
                    .bucket(ossProperties.getBucketName())
                    .key(objectKey)
                    .build();

            DeleteObjectResult response = ossClient.deleteObject(request);
            log.info("文件删除成功: objectKey={}", objectKey);
            return true;

        } catch (Exception e) {
            log.error("从 OSS 删除文件失败: objectKey={}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean doesObjectExist(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.newBuilder()
                    .bucket(ossProperties.getBucketName())
                    .key(objectKey)
                    .build();

            ossClient.headObject(request);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, long expirationSeconds) {
        GetObjectRequest request = GetObjectRequest.newBuilder()
                .bucket(ossProperties.getBucketName())
                .key(objectKey)
                .build();
        PresignOptions options = PresignOptions.newBuilder().expiration(Duration.ofSeconds(expirationSeconds)).build();
        PresignResult presignResult = ossClient.presign(request,options);
        return presignResult.url();
    }
}
