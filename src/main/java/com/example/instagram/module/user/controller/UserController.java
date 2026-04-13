package com.example.instagram.module.user.controller;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.result.Result;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.user.dto.UpdateProfileDTO;
import com.example.instagram.module.user.service.UserService;
import com.example.instagram.module.user.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @Operation(summary = "获取指定用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestParam Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/stats")
    public Result<UserStatsVO> getUserStats(@RequestParam Long userId) {
        return Result.success(userService.getUserStats(userId));
    }

    @Operation(summary = "编辑个人资料")
    @PostMapping("/profile/update")
    public Result<UserInfoVO> updateProfile(@RequestBody UpdateProfileDTO dto) {
        return Result.success("资料更新成功", userService.updateProfile(dto));
    }

    @Operation(summary = "获取用户帖子列表")
    @GetMapping("/posts")
    public Result<PageResult<UserPostGridVO>> listUserPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "18") Integer pageSize,
            @RequestParam(required = false) String mediaType) {
        return Result.success(userService.listUserPosts(userId, page, pageSize, mediaType));
    }

    @Operation(summary = "获取用户视频列表")
    @GetMapping("/reels")
    public Result<PageResult<UserReelVO>> listUserReels(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "18") Integer pageSize) {
        return Result.success(userService.listUserReels(userId, page, pageSize));
    }

    @Operation(summary = "获取用户帖子详情列表（游标加载）")
    @GetMapping("/posts/detail")
    public Result<PageResult<PostFeedVO>> listUserPostsDetail(
            @RequestParam Long userId,
            @RequestParam Long postId,
            @RequestParam(required = false) String direction) {
        return Result.success(userService.listUserPostsDetail(userId, postId, direction));
    }

    @Operation(summary = "获取推荐用户列表")
    @GetMapping("/discover")
    public Result<List<DiscoverUserVO>> listDiscoverUsers(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(userService.listDiscoverUsers(limit));
    }
}
