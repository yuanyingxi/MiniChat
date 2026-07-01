package com.minichat.user.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.minichat.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private OSS ossClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserIndexService userIndexService;

    @InjectMocks
    private FileService fileService;

    private Long userId = 1L;

    /** 生成带 JPEG 魔数头的 Mock 文件 */
    private MockMultipartFile jpegFile(String name, byte[] extraData) {
        // JPEG 头: FF D8 FF + 原始数据
        byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        byte[] full = new byte[jpegHeader.length + extraData.length];
        System.arraycopy(jpegHeader, 0, full, 0, jpegHeader.length);
        System.arraycopy(extraData, 0, full, jpegHeader.length, extraData.length);
        return new MockMultipartFile("file", name, "image/jpeg", full);
    }

    /** 生成带 PNG 魔数头的 Mock 文件 */
    private MockMultipartFile pngFile(String name, byte[] extraData) {
        // PNG 头: 89 50 4E 47
        byte[] pngHeader = {(byte) 0x89, 0x50, 0x4E, 0x47};
        byte[] full = new byte[pngHeader.length + extraData.length];
        System.arraycopy(pngHeader, 0, full, 0, pngHeader.length);
        System.arraycopy(extraData, 0, full, pngHeader.length, extraData.length);
        return new MockMultipartFile("file", name, "image/png", full);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucketName", "minichat-avatars");
        ReflectionTestUtils.setField(fileService, "endpoint", "oss-cn-hangzhou.aliyuncs.com");
    }

    @Test
    void uploadAvatar_ShouldSucceed_WhenValidImage() throws IOException {
        MultipartFile file = jpegFile("avatar.jpg", "fake-image-data".getBytes());

        // userMapper.selectById 需返回 User（ES 同步用）
        when(userMapper.selectById(userId)).thenReturn(new com.minichat.user.entity.User());

        String url = fileService.uploadAvatar(userId, file);

        assertTrue(url.startsWith("https://minichat-avatars.oss-cn-hangzhou.aliyuncs.com/avatars/1/"));
        assertTrue(url.endsWith(".jpg"));
        verify(ossClient, times(1)).putObject(any(PutObjectRequest.class));
        verify(userMapper, atLeastOnce()).updateById(isA(com.minichat.user.entity.User.class));
    }

    @Test
    void uploadAvatar_ShouldThrow_WhenFileEmpty() {
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadAvatar(userId, file));
        assertEquals("请选择文件", ex.getMessage());
        verify(ossClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadAvatar_ShouldThrow_WhenNotImage() {
        MultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf",
                "fake-pdf-data".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadAvatar(userId, file));
        assertEquals("只允许上传图片文件", ex.getMessage());
        verify(ossClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadAvatar_ShouldThrow_WhenContentTypeNull() {
        MultipartFile file = new MockMultipartFile(
                "file", "file.bin", null,
                "binary-data".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadAvatar(userId, file));
        assertEquals("只允许上传图片文件", ex.getMessage());
    }

    @Test
    void uploadAvatar_ShouldThrow_WhenFileTooLarge() {
        byte[] largeData = new byte[3 * 1024 * 1024]; // 3MB
        MultipartFile file = jpegFile("large.jpg", largeData);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadAvatar(userId, file));
        assertEquals("头像文件不能超过 2MB", ex.getMessage());
        verify(ossClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadAvatar_ShouldThrow_WhenOssFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("avatar.jpg");
        // 第一次调用返回 JPEG 魔数，第二次返回完整数据给 OSS
        byte[] jpegHeader = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(jpegHeader))
                .thenReturn(new ByteArrayInputStream(jpegHeader));

        doThrow(new RuntimeException("OSS connection failed"))
                .when(ossClient).putObject(any(PutObjectRequest.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadAvatar(userId, file));
        assertTrue(ex.getMessage().contains("头像上传失败"));
    }

    @Test
    void uploadAvatar_ShouldUseDefaultExtension_WhenFilenameHasNoExtension() {
        MultipartFile file = pngFile("avatar", "data".getBytes());

        when(userMapper.selectById(userId)).thenReturn(new com.minichat.user.entity.User());

        String url = fileService.uploadAvatar(userId, file);

        assertTrue(url.endsWith(".jpg")); // 默认后缀
    }

    @Test
    void uploadAvatar_ShouldPreserveExtension_WhenFilenameHasExtension() {
        MultipartFile file = pngFile("avatar.png", "data".getBytes());

        when(userMapper.selectById(userId)).thenReturn(new com.minichat.user.entity.User());

        String url = fileService.uploadAvatar(userId, file);

        assertTrue(url.endsWith(".png"));
    }
}
