package com.example.instagram.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {

    LIKE("like", "点赞"),
    COMMENT("comment", "评论"),
    FOLLOW("follow", "关注"),
    MENTION("mention", "提及");

    private final String value;
    private final String desc;
}
