package com.example.instagram.module.post.vo;

import lombok.Data;

import java.util.List;

@Data
public class PostFeedVO {

    private String postId;
    private String userId;
    private String username;
    private String avatar;
    private Boolean isVerified;
    private String location;
    private String content;
    private String mediaType;
    private List<MediaItemVO> mediaList;
    private Integer mediaCount;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer sharesCount;
    private Boolean isLiked;
    private Boolean isSaved;
    private Boolean isFollowing;
    private Long createdAt;
}
