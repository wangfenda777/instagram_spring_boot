package com.example.instagram.module.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.dto.FollowDTO;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.follow.service.FollowService;
import com.example.instagram.module.follow.vo.FollowUserVO;
import com.example.instagram.module.follow.vo.FollowVO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public FollowVO follow(FollowDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long targetUserId = dto.getUserId();

        if (currentUserId.equals(targetUserId)) {
            throw BusinessException.badRequest("不能关注自己");
        }

        // 检查是否已关注
        Long count = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId)
                .eq(Follow::getFollowingId, targetUserId));
        if (count > 0) {
            throw BusinessException.conflict("已关注该用户");
        }

        Follow follow = new Follow();
        follow.setFollowerId(currentUserId);
        follow.setFollowingId(targetUserId);
        followMapper.insert(follow);

        // 更新冗余统计
        updateFollowCount(currentUserId, targetUserId, 1);

        FollowVO vo = new FollowVO();
        vo.setIsFollowing(true);
        return vo;
    }

    @Override
    @Transactional
    public FollowVO unfollow(FollowDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long targetUserId = dto.getUserId();

        followMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId)
                .eq(Follow::getFollowingId, targetUserId));

        // 更新冗余统计
        updateFollowCount(currentUserId, targetUserId, -1);

        FollowVO vo = new FollowVO();
        vo.setIsFollowing(false);
        return vo;
    }

    @Override
    public PageResult<FollowUserVO> pageFollowers(Long userId, Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();
        Page<Follow> followPage = new Page<>(page, pageSize);
        followMapper.selectPage(followPage, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowingId, userId)
                .orderByDesc(Follow::getCreatedAt));

        List<Long> followerIds = followPage.getRecords().stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
        if (followerIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), followPage.getTotal(), page, pageSize);
        }

        List<User> users = userMapper.selectBatchIds(followerIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Set<Long> followingIds = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, currentUserId)
                        .in(Follow::getFollowingId, followerIds))
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        List<FollowUserVO> list = followerIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> toFollowUserVO(user, followingIds.contains(user.getId())))
                .collect(Collectors.toList());

        return PageResult.of(list, followPage.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<FollowUserVO> pageFollowing(Long userId, Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();
        Page<Follow> followPage = new Page<>(page, pageSize);
        followMapper.selectPage(followPage, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, userId)
                .orderByDesc(Follow::getCreatedAt));

        List<Long> followingIds = followPage.getRecords().stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());
        if (followingIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), followPage.getTotal(), page, pageSize);
        }

        List<User> users = userMapper.selectBatchIds(followingIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Set<Long> currentUserFollowingIds = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, currentUserId)
                        .in(Follow::getFollowingId, followingIds))
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        List<FollowUserVO> list = followingIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> toFollowUserVO(user, currentUserFollowingIds.contains(user.getId())))
                .collect(Collectors.toList());

        return PageResult.of(list, followPage.getTotal(), page, pageSize);
    }

    private void updateFollowCount(Long followerId, Long followingId, int delta) {
        User follower = userMapper.selectById(followerId);
        if (follower != null) {
            follower.setFollowingCount(Math.max(0, follower.getFollowingCount() + delta));
            userMapper.updateById(follower);
        }

        User following = userMapper.selectById(followingId);
        if (following != null) {
            following.setFollowersCount(Math.max(0, following.getFollowersCount() + delta));
            userMapper.updateById(following);
        }
    }

    private FollowUserVO toFollowUserVO(User user, boolean isFollowing) {
        FollowUserVO vo = new FollowUserVO();
        vo.setUserId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setAvatar(user.getAvatar());
        vo.setIsVerified(user.getIsVerified() != null && user.getIsVerified() == 1);
        vo.setIsFollowing(isFollowing);
        return vo;
    }
}
