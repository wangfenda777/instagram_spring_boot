package com.example.instagram.module.post.controller;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.result.Result;
import com.example.instagram.module.post.dto.PostIdDTO;
import com.example.instagram.module.post.service.PostService;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.post.vo.PostLikeVO;
import com.example.instagram.module.post.vo.PostSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
@Tag(name = "帖子管理")
public class PostController {

    @Autowired
    private PostService postService;

    @Operation(summary = "获取首页帖子 Feed 流")
    @GetMapping("/feed")
    public Result<PageResult<PostFeedVO>> pagePostFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.pagePostFeed(page, pageSize));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/detail")
    public Result<PostFeedVO> getPostDetail(@RequestParam Long postId) {
        return Result.success(postService.getPostDetail(postId));
    }

    @Operation(summary = "点赞帖子")
    @PostMapping("/like")
    public Result<PostLikeVO> likePost(@Valid @RequestBody PostIdDTO dto) {
        return Result.success("点赞成功", postService.likePost(dto));
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public Result<PostLikeVO> unlikePost(@Valid @RequestBody PostIdDTO dto) {
        return Result.success("取消点赞", postService.unlikePost(dto));
    }

    @Operation(summary = "收藏帖子")
    @PostMapping("/save")
    public Result<PostSaveVO> savePost(@Valid @RequestBody PostIdDTO dto) {
        return Result.success("收藏成功", postService.savePost(dto));
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/unsave")
    public Result<PostSaveVO> unsavePost(@Valid @RequestBody PostIdDTO dto) {
        return Result.success("取消收藏", postService.unsavePost(dto));
    }
}
