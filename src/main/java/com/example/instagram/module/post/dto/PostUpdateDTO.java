package com.example.instagram.module.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "编辑帖子参数")
public class PostUpdateDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "帖子文案")
    private String content;

    @Schema(description = "位置")
    private String location;
}
