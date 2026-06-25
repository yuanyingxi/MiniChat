package com.minichat.message.controller;

import com.minichat.message.dto.UploadVO;
import com.minichat.message.service.MessageService;
import com.minichat.message.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @PostMapping("/upload")
    public UploadVO upload(@RequestParam("file") MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String url = ossService.uploadFile(file, generateObjectKey(file));

        return new UploadVO(url);
    }

    private String generateObjectKey(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        String suffix = "";

        if (fileName != null && fileName.contains(".")) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }

        String folder = "other";

        String contentType = file.getContentType();

        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                folder = "image";
            } else if (contentType.startsWith("video/")) {
                folder = "video";
            } else if (contentType.startsWith("audio/")) {
                folder = "voice";
            }
        }

        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return String.format(
                "chat/%s/%s/%s%s",
                folder,
                date,
                UUID.randomUUID(),
                suffix
        );
    }
}
