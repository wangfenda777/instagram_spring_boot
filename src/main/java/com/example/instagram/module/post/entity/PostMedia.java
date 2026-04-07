package com.example.instagram.module.post.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_media")
public class PostMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("media_url")
    private String mediaUrl;

    @TableField("media_type")
    private String mediaType;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer duration;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
