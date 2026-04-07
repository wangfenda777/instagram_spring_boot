package com.example.instagram.module.post.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("posts")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String content;

    private String location;

    @TableField("media_type")
    private String mediaType;

    @TableField("media_count")
    private Integer mediaCount;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("likes_count")
    private Integer likesCount;

    @TableField("comments_count")
    private Integer commentsCount;

    @TableField("shares_count")
    private Integer sharesCount;

    @TableField("views_count")
    private Integer viewsCount;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
