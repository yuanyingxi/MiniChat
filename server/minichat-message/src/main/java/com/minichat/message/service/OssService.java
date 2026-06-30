package com.minichat.message.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface OssService {
    /**
     * 上传文件（通过 MultipartFile）
     * @param file 上传的文件
     * @param objectKey 对象键名（路径）
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file, String objectKey);

    /**
     * 上传文件（通过 InputStream）
     * @param inputStream 文件输入流
     * @param objectKey 对象键名
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    String uploadFile(InputStream inputStream, String objectKey, String contentType);

    /**
     * 下载文件
     * @param objectKey 对象键名
     * @return 文件输入流
     */
    InputStream downloadFile(String objectKey);

    /**
     * 删除文件
     * @param objectKey 对象键名
     * @return 是否删除成功
     */
    boolean deleteFile(String objectKey);

    /**
     * 检查文件是否存在
     * @param objectKey 对象键名
     * @return 是否存在
     */
    boolean doesObjectExist(String objectKey);

    /**
     * 获取文件访问 URL
     * @param objectKey 对象键名
     * @param expirationSeconds 过期时间（秒）
     * @return 签名 URL
     */
    String generatePresignedUrl(String objectKey, long expirationSeconds);
}
