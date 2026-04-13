package com.example.instagram.module.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布帖子参数")
public class PostCreateDTO {

    @Schema(description = "帖子文案")
    private String content;

    @Schema(description = "位置")
    private String location;

    @NotBlank(message = "媒体类型不能为空")
    @Schema(description = "媒体类型: image/video")
    private String mediaType;

    @Schema(description = "封面图URL（视频帖子使用）")
    private String coverUrl;

    @NotEmpty(message = "媒体列表不能为空")
    @Schema(description = "媒体URL列表")
    private List<String> mediaUrls;
}
