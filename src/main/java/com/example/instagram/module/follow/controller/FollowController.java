package com.example.instagram.module.follow.controller;

import com.example.instagram.common.result.Result;
import com.example.instagram.module.follow.dto.FollowDTO;
import com.example.instagram.module.follow.service.FollowService;
import com.example.instagram.module.follow.vo.FollowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "关注管理")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow")
    public Result<FollowVO> follow(@Valid @RequestBody FollowDTO dto) {
        return Result.success("关注成功", followService.follow(dto));
    }

    @Operation(summary = "取消关注")
    @PostMapping("/unfollow")
    public Result<FollowVO> unfollow(@Valid @RequestBody FollowDTO dto) {
        return Result.success("取消关注成功", followService.unfollow(dto));
    }
}
