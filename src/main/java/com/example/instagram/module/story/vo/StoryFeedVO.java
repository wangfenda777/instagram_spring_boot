package com.example.instagram.module.story.vo;

import lombok.Data;

@Data
public class StoryFeedVO {

    private String storyId;
    private String userId;
    private String username;
    private String avatar;
    private Boolean hasUnread;
    private Long expiredAt;
}
