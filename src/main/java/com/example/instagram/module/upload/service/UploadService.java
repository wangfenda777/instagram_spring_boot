package com.example.instagram.module.upload.service;

import com.example.instagram.module.upload.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    UploadVO uploadImage(MultipartFile file);
}
