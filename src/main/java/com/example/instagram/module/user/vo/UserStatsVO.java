package com.example.instagram.module.user.vo;

import lombok.Data;

@Data
public class UserStatsVO {

    private String userId;
    private Integer postsCount;
    private Integer followersCount;
    private Integer followingCount;
}
