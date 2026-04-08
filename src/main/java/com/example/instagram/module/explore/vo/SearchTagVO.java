package com.example.instagram.module.explore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签搜索结果")
public class SearchTagVO {

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "标签名称，含#前缀")
    private String name;

    @Schema(description = "热度")
    private Integer heat;

    @Schema(description = "关联帖子数")
    private Integer postCount;
}
