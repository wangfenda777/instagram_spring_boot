package com.example.instagram.module.upload.service.impl;

import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.module.upload.service.UploadService;
import com.example.instagram.module.upload.vo.UploadVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".mov", ".avi", ".webm"
    );

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 50L * 1024 * 1024; // 50MB

    @Override
    public UploadVO uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !hasAllowedExtension(originalName, ALLOWED_IMAGE_EXTENSIONS)) {
            throw BusinessException.badRequest("仅支持 jpg/png/gif/webp 格式的图片");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw BusinessException.badRequest("图片文件不能超过 10MB");
        }

        return saveFile(file, "images");
    }

    @Override
    public UploadVO uploadVideo(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !hasAllowedExtension(originalName, ALLOWED_VIDEO_EXTENSIONS)) {
            throw BusinessException.badRequest("仅支持 mp4/mov/avi/webm 格式的视频");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw BusinessException.badRequest("视频文件不能超过 50MB");
        }

        return saveFile(file, "videos");
    }

    private boolean hasAllowedExtension(String filename, Set<String> allowedExtensions) {
        String lowerName = filename.toLowerCase();
        return allowedExtensions.stream().anyMatch(lowerName::endsWith);
    }

    private UploadVO saveFile(MultipartFile file, String subDir) {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadPath + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            throw BusinessException.badRequest("文件上传失败");
        }

        UploadVO vo = new UploadVO();
        vo.setUrl(urlPrefix + "/" + subDir + "/" + fileName);
        vo.setOriginalName(originalName);
        return vo;
    }
}
