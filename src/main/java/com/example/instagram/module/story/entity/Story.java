package com.example.instagram.module.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stories")
public class Story {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("media_url")
    private String mediaUrl;

    @TableField("media_type")
    private String mediaType;

    private Integer duration;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
