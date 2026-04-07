package com.example.instagram.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    private String phone;

    private String password;

    @TableField("display_name")
    private String displayName;

    private String avatar;

    private String bio;

    @TableField("is_verified")
    private Integer isVerified;

    @TableField("is_private")
    private Integer isPrivate;

    @TableField("posts_count")
    private Integer postsCount;

    @TableField("followers_count")
    private Integer followersCount;

    @TableField("following_count")
    private Integer followingCount;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
