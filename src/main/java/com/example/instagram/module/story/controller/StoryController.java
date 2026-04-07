package com.example.instagram.module.story.controller;

import com.example.instagram.common.result.Result;
import com.example.instagram.module.story.service.StoryService;
import com.example.instagram.module.story.vo.StoryFeedVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/story")
@Tag(name = "快拍管理")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Operation(summary = "获取快拍列表")
    @GetMapping("/feed")
    public Result<List<StoryFeedVO>> listStoryFeed() {
        return Result.success(storyService.listStoryFeed());
    }
}
