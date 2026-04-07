package com.example.instagram.module.upload.controller;

import com.example.instagram.common.result.Result;
import com.example.instagram.module.upload.service.UploadService;
import com.example.instagram.module.upload.vo.UploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Operation(summary = "上传图片")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UploadVO> uploadImage(@RequestPart("file") MultipartFile file) {
        return Result.success("上传成功", uploadService.uploadImage(file));
    }
}
