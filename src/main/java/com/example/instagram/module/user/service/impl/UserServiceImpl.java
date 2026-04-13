package com.example.instagram.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.notification.mapper.NotificationMapper;
import com.example.instagram.module.notification.entity.Notification;
import com.example.instagram.module.post.entity.Post;
import com.example.instagram.module.post.mapper.PostMapper;
import com.example.instagram.module.post.service.PostService;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.user.dto.UpdateProfileDTO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import com.example.instagram.module.user.service.UserService;
import com.example.instagram.module.user.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        UserInfoVO vo = toUserInfoVO(user);
        // 查询是否有未读通知
        Long unreadCount = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        vo.setHasNotification(unreadCount > 0);
        return vo;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        UserInfoVO vo = toUserInfoVO(user);
        Long currentUserId = UserContext.getCurrentUserId();
        vo.setIsFollowing(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId)
                .eq(Follow::getFollowingId, userId)) > 0);
        return vo;
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        UserStatsVO vo = new UserStatsVO();
        vo.setUserId(String.valueOf(userId));
        vo.setPostsCount(user.getPostsCount());
        vo.setFollowersCount(user.getFollowersCount());
        vo.setFollowingCount(user.getFollowingCount());
        return vo;
    }

    @Override
    public UserInfoVO updateProfile(UpdateProfileDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }

        userMapper.updateById(user);
        return toUserInfoVO(user);
    }

    @Override
    public PageResult<UserPostGridVO> listUserPosts(Long userId, Integer page, Integer pageSize, String mediaType) {
        Page<Post> postPage = new Page<>(page, pageSize);
        postMapper.selectPage(postPage, new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(mediaType != null && !mediaType.isEmpty(), Post::getMediaType, mediaType)
                .orderByDesc(Post::getCreatedAt));

        List<UserPostGridVO> list = postPage.getRecords().stream().map(post -> {
            UserPostGridVO vo = new UserPostGridVO();
            vo.setPostId(String.valueOf(post.getId()));
            vo.setCoverUrl(post.getCoverUrl());
            vo.setMediaType(post.getMediaType());
            vo.setMediaCount(post.getMediaCount());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(list, postPage.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<UserReelVO> listUserReels(Long userId, Integer page, Integer pageSize) {
        Page<Post> postPage = new Page<>(page, pageSize);
        postMapper.selectPage(postPage, new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getMediaType, "video")
                .orderByDesc(Post::getCreatedAt));

        List<UserReelVO> list = postPage.getRecords().stream().map(post -> {
            UserReelVO vo = new UserReelVO();
            vo.setPostId(String.valueOf(post.getId()));
            vo.setCoverUrl(post.getCoverUrl());
            vo.setViewsCount(post.getViewsCount());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(list, postPage.getTotal(), page, pageSize);
    }

    @Override
    public List<DiscoverUserVO> listDiscoverUsers(Integer limit) {
        Long currentUserId = UserContext.getCurrentUserId();

        // 获取已关注的用户 ID 列表
        List<Follow> follows = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId));
        Set<Long> followingIds = follows.stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());
        followingIds.add(currentUserId); // 排除自己

        // 查询推荐用户（简单实现：查询未关注的用户）
        Page<User> userPage = new Page<>(1, limit);
        userMapper.selectPage(userPage, new LambdaQueryWrapper<User>()
                .notIn(User::getId, followingIds)
                .orderByDesc(User::getFollowersCount));

        return userPage.getRecords().stream().map(user -> {
            DiscoverUserVO vo = new DiscoverUserVO();
            vo.setUserId(String.valueOf(user.getId()));
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
            vo.setTag("为你推荐");
            vo.setIsFollowing(false);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<PostFeedVO> listUserPostsDetail(Long userId, Long postId, String direction) {
        Long currentUserId = UserContext.getCurrentUserId();

        List<Post> posts;
        boolean hasMore;

        if (direction == null || direction.isEmpty()) {
            // 初次加载：本条 + 比它更老的5条
            Post currentPost = postMapper.selectById(postId);

            List<Post> afterPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .eq(Post::getUserId, userId)
                    .lt(Post::getId, postId)
                    .orderByDesc(Post::getId)
                    .last("LIMIT 5"));

            posts = new ArrayList<>();
            if (currentPost != null && currentPost.getUserId().equals(userId)) {
                posts.add(currentPost);
            }
            posts.addAll(afterPosts);

            hasMore = afterPosts.size() == 5;

        } else if ("before".equalsIgnoreCase(direction)) {
            // 往上加载：比 postId 更新的5条
            posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .eq(Post::getUserId, userId)
                    .gt(Post::getId, postId)
                    .orderByAsc(Post::getId)
                    .last("LIMIT 5"));
            java.util.Collections.reverse(posts);
            hasMore = posts.size() == 5;

        } else {
            // 往下加载：比 postId 更老的5条
            posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .eq(Post::getUserId, userId)
                    .lt(Post::getId, postId)
                    .orderByDesc(Post::getId)
                    .last("LIMIT 5"));
            hasMore = posts.size() == 5;
        }

        List<PostFeedVO> list = posts.stream()
                .map(post -> {
                    PostFeedVO vo = postService.buildPostFeedVO(post, currentUserId);
                    vo.setIsFollowing(null);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult<PostFeedVO> result = new PageResult<>();
        result.setList(list);
        result.setHasMore(hasMore);
        return result;
    }

    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setIsVerified(user.getIsVerified() != null && user.getIsVerified() == 1);
        vo.setIsPrivate(user.getIsPrivate() != null && user.getIsPrivate() == 1);
        return vo;
    }
}
