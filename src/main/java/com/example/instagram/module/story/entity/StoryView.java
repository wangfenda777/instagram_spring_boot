package com.example.instagram.module.story.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("story_views")
public class StoryView {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("story_id")
    private Long storyId;

    @TableField("user_id")
    private Long userId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
