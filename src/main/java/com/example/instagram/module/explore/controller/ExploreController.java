package com.example.instagram.module.explore.controller;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.result.Result;
import com.example.instagram.module.explore.service.ExploreService;
import com.example.instagram.module.explore.vo.ExploreItemVO;
import com.example.instagram.module.explore.vo.SearchTagVO;
import com.example.instagram.module.explore.vo.SearchUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "探索与搜索")
public class ExploreController {

    @Autowired
    private ExploreService exploreService;

    @Operation(summary = "获取探索页推荐内容")
    @GetMapping("/explore/feed")
    public Result<PageResult<ExploreItemVO>> pageExploreFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "24") Integer pageSize) {
        return Result.success(exploreService.pageExploreFeed(page, pageSize));
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search/user")
    public Result<PageResult<SearchUserVO>> searchUser(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(exploreService.searchUser(keyword, page, pageSize));
    }

    @Operation(summary = "搜索标签")
    @GetMapping("/search/tag")
    public Result<PageResult<SearchTagVO>> searchTag(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(exploreService.searchTag(keyword, page, pageSize));
    }

    @Operation(summary = "搜索帖子")
    @GetMapping("/search/post")
    public Result<PageResult<ExploreItemVO>> searchPost(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "24") Integer pageSize) {
        return Result.success(exploreService.searchPost(keyword, type, page, pageSize));
    }
}
