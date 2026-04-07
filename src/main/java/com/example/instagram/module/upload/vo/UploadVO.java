package com.example.instagram.module.upload.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "上传结果")
public class UploadVO {

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "原始文件名")
    private String originalName;
}
