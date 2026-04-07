package com.example.instagram.module.story.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.story.entity.Story;
import com.example.instagram.module.story.entity.StoryView;
import com.example.instagram.module.story.mapper.StoryMapper;
import com.example.instagram.module.story.mapper.StoryViewMapper;
import com.example.instagram.module.story.service.StoryService;
import com.example.instagram.module.story.vo.StoryFeedVO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryMapper storyMapper;

    @Autowired
    private StoryViewMapper storyViewMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<StoryFeedVO> listStoryFeed() {
        Long currentUserId = UserContext.getCurrentUserId();

        // 获取关注的用户列表
        List<Follow> follows = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId));
        List<Long> followingIds = follows.stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());

        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取未过期的快拍
        List<Story> stories = storyMapper.selectList(new LambdaQueryWrapper<Story>()
                .in(Story::getUserId, followingIds)
                .gt(Story::getExpiredAt, LocalDateTime.now())
                .orderByDesc(Story::getCreatedAt));

        if (stories.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取当前用户已读的快拍ID
        List<Long> storyIds = stories.stream().map(Story::getId).collect(Collectors.toList());
        List<StoryView> views = storyViewMapper.selectList(new LambdaQueryWrapper<StoryView>()
                .eq(StoryView::getUserId, currentUserId)
                .in(StoryView::getStoryId, storyIds));
        Set<Long> viewedStoryIds = views.stream()
                .map(StoryView::getStoryId)
                .collect(Collectors.toSet());

        // 按用户分组，每个用户取最新一条
        Map<Long, Story> latestByUser = new LinkedHashMap<>();
        Map<Long, Boolean> hasUnreadByUser = new HashMap<>();
        for (Story story : stories) {
            latestByUser.putIfAbsent(story.getUserId(), story);
            if (!viewedStoryIds.contains(story.getId())) {
                hasUnreadByUser.put(story.getUserId(), true);
            }
        }

        return latestByUser.entrySet().stream().map(entry -> {
            Long userId = entry.getKey();
            Story story = entry.getValue();
            User user = userMapper.selectById(userId);

            StoryFeedVO vo = new StoryFeedVO();
            vo.setStoryId(String.valueOf(story.getId()));
            vo.setUserId(String.valueOf(userId));
            vo.setUsername(user != null ? user.getUsername() : "");
            vo.setAvatar(user != null ? user.getAvatar() : "");
            vo.setHasUnread(hasUnreadByUser.getOrDefault(userId, false));
            vo.setExpiredAt(story.getExpiredAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return vo;
        }).collect(Collectors.toList());
    }
}
