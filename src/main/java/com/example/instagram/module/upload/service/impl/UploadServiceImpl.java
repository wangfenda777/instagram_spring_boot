package com.example.instagram.module.upload.service.impl;

import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.module.upload.service.UploadService;
import com.example.instagram.module.upload.vo.UploadVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public UploadVO uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw BusinessException.badRequest("仅支持 jpg/png/gif/webp 格式的图片");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            throw BusinessException.badRequest("文件上传失败");
        }

        UploadVO vo = new UploadVO();
        vo.setUrl(urlPrefix + "/" + fileName);
        vo.setOriginalName(originalName);
        return vo;
    }
}
