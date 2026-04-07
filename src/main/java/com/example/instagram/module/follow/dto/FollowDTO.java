package com.example.instagram.module.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
