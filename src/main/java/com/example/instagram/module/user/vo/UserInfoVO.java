package com.example.instagram.module.user.vo;

import lombok.Data;

@Data
public class UserInfoVO {

    private String userId;
    private String username;
    private String displayName;
    private String avatar;
    private String bio;
    private Boolean isVerified;
    private Boolean isPrivate;
    private Boolean hasNotification;
    private Boolean isFollowing;
}
