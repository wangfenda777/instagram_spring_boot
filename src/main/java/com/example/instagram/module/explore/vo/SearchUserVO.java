package com.example.instagram.module.explore.vo;

import lombok.Data;

@Data
public class SearchUserVO {

    private String userId;
    private String username;
    private String displayName;
    private String avatar;
    private Boolean isVerified;
    private Integer followersCount;
    private Boolean isFollowing;
}
