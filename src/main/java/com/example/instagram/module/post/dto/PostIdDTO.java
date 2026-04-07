package com.example.instagram.module.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostIdDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;
}
