package com.example.instagram.module.follow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关注/粉丝用户信息")
public class FollowUserVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "显示名称")
    private String displayName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "是否认证")
    private Boolean isVerified;

    @Schema(description = "当前登录用户是否已关注该用户")
    private Boolean isFollowing;
}
