package com.example.instagram.module.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.dto.FollowDTO;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.follow.service.FollowService;
import com.example.instagram.module.follow.vo.FollowVO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
