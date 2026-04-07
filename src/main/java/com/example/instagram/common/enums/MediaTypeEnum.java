package com.example.instagram.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaTypeEnum {

    IMAGE("image", "图片"),
    VIDEO("video", "视频");

    private final String value;
    private final String desc;
}
