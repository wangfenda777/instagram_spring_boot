package com.example.instagram.module.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_tag")
public class PostTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("tag_id")
    private Long tagId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
