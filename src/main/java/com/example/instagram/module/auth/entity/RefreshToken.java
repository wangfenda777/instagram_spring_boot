package com.example.instagram.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refresh_tokens")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String token;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("is_revoked")
    private Integer isRevoked;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
