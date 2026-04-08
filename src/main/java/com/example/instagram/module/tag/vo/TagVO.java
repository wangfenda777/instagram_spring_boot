package com.example.instagram.module.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签信息")
public class TagVO {

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "标签名称，含#前缀")
    private String name;

    @Schema(description = "热度")
    private Integer heat;

    @Schema(description = "关联帖子数")
    private Integer postCount;
}
