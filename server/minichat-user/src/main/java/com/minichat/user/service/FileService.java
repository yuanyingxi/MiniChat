package com.minichat.user.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserIndexService userIndexService;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    // 上传头像到 OSS
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new RuntimeException("请选择文件");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只允许上传图片文件");
        }

        // 3. 校验文件大小（最大 2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("头像文件不能超过 2MB");
        }

        // 4. 校验文件内容（魔数校验，防止伪造 Content-Type）
        if (!isImageByMagicBytes(file)) {
            throw new RuntimeException("文件内容不是有效图片");
        }

        try {
            // 4. 生成唯一文件名，按用户ID归类
            String ext = getExtension(file.getOriginalFilename());
            String fileName = "avatars/" + userId + "/" + UUID.randomUUID() + ext;

            // 5. 上传到 OSS
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file.getInputStream());
            ossClient.putObject(request);

            // 6. 构造可访问的 URL（兼容带协议和不带协议的 endpoint 格式）
            String cleanEndpoint = endpoint.replaceAll("https?://", "");
            String url = "https://" + bucketName + "." + cleanEndpoint + "/" + fileName;

            // 7. 更新用户的 avatar 字段
            User user = new User();
            user.setId(userId);
            user.setAvatar(url);
            userMapper.updateById(user);

            // 8. 同步 ES 索引（搜索时能看到最新头像）
            User fullUser = userMapper.selectById(userId);
            userIndexService.indexUser(fullUser);

            return url;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // 默认后缀
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 通过文件头魔数判断是否为真实图片
     */
    private boolean isImageByMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 3) return false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
            // GIF: 47 49 46 38
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
