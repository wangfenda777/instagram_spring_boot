package com.example.instagram.module.user.vo;

import lombok.Data;

@Data
public class DiscoverUserVO {

    private String userId;
    private String username;
    private String avatar;
    private String tag;
    private Boolean isFollowing;
}
