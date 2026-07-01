package com.minichat;

import com.minichat.message.service.OssService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OssServiceImplIntegrationTest {

    @Autowired
    private OssService ossService;   // 注入你要测试的实现类

    private String testKey;          // 每个测试方法使用唯一的 key

    @BeforeEach
    void setUp() {
        testKey = "integration-test/" + UUID.randomUUID() + ".txt";
    }

    @AfterEach
    void tearDown() {
        // 清理：删除本次测试可能残留的文件
        try {
            ossService.deleteFile(testKey);
        } catch (Exception ignored) {}
    }

    // ========== 测试 uploadFile(MultipartFile) ==========
    @Test
    @Order(1)
    void testUploadFileWithMultipartFile() throws Exception {
        byte[] content = "Hello from MultipartFile".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", content);

        String url = ossService.uploadFile(multipartFile, testKey);
        assertThat(url).isNotNull();
        assertThat(url).contains(testKey);

        // 验证文件存在
        assertTrue(ossService.doesObjectExist(testKey));

        // 验证下载内容一致
        try (InputStream is = ossService.downloadFile(testKey)) {
            byte[] downloaded = is.readAllBytes();
            assertThat(downloaded).isEqualTo(content);
        }
    }

    // ========== 测试 uploadFile(InputStream) ==========
    @Test
    @Order(2)
    void testUploadFileWithInputStream() throws Exception {
        byte[] content = "Hello from InputStream".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);

        String url = ossService.uploadFile(inputStream, testKey, "text/plain");
        assertThat(url).isNotNull();
        assertTrue(ossService.doesObjectExist(testKey));

        try (InputStream downloaded = ossService.downloadFile(testKey)) {
            assertThat(downloaded.readAllBytes()).isEqualTo(content);
        }
    }

    // ========== 测试 downloadFile ==========
    @Test
    @Order(3)
    void testDownloadFile() throws Exception {
        // 先上传一个文件
        String expectedContent = "Download test content";
        try (InputStream is = new ByteArrayInputStream(expectedContent.getBytes())) {
            ossService.uploadFile(is, testKey, "text/plain");
        }

        // 下载并验证
        try (InputStream downloaded = ossService.downloadFile(testKey)) {
            String actualContent = new String(downloaded.readAllBytes());
            assertThat(actualContent).isEqualTo(expectedContent);
        }
    }

    // ========== 测试 deleteFile ==========
    @Test
    @Order(4)
    void testDeleteFile() {
        // 先上传
        ossService.uploadFile(new ByteArrayInputStream("to delete".getBytes()), testKey, "text/plain");
        assertTrue(ossService.doesObjectExist(testKey));

        // 删除
        boolean deleted = ossService.deleteFile(testKey);
        assertTrue(deleted);

        // 验证不存在
        assertFalse(ossService.doesObjectExist(testKey));
    }

    // ========== 测试 doesObjectExist ==========
    @Test
    @Order(5)
    void testDoesObjectExist() {
        // 不存在时
        assertFalse(ossService.doesObjectExist(testKey));

        // 上传后存在
        ossService.uploadFile(new ByteArrayInputStream("exists".getBytes()), testKey, "text/plain");
        assertTrue(ossService.doesObjectExist(testKey));
    }

    // ========== 测试 generatePresignedUrl ==========
    @Test
    @Order(6)
    void testGeneratePresignedUrl() throws IOException {
        // 先上传一个文件
        byte[] originalContent = "presigned".getBytes();
        ossService.uploadFile(new ByteArrayInputStream(originalContent), testKey, "text/plain");

        // 生成 URL（有效期 10 秒）
        String presignedUrl = ossService.generatePresignedUrl(testKey, 100);
        System.out.println("Full URL: " + presignedUrl);
        assertThat(presignedUrl).isNotNull();
        assertThat(presignedUrl).startsWith("https://");

        // 验证
        URL url = new URL(presignedUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        conn.setInstanceFollowRedirects(true);  // 允许重定向（某些情况下 OSS 会返回 302）

        int responseCode = conn.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);
        if (responseCode != 200) {
            // 读取错误流，打印详细错误
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    String errorBody = new String(err.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("Error Body: " + errorBody);
                }
            }
        }
        assertEquals(200, responseCode, "预签名 URL 应返回 200");

        // 读取响应体
        byte[] downloaded;
        try (InputStream is = conn.getInputStream()) {
            downloaded = is.readAllBytes();
        }
        assertArrayEquals(originalContent, downloaded, "下载内容应与上传内容一致");

        conn.disconnect();
    }
}