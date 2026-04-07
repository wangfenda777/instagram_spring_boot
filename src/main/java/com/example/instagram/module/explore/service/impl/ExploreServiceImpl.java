package com.example.instagram.module.explore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.explore.service.ExploreService;
import com.example.instagram.module.explore.vo.ExploreItemVO;
import com.example.instagram.module.explore.vo.SearchUserVO;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.post.entity.Post;
import com.example.instagram.module.post.mapper.PostMapper;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExploreServiceImpl implements ExploreService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Override
    public PageResult<ExploreItemVO> pageExploreFeed(Integer page, Integer pageSize) {
        Page<Post> postPage = new Page<>(page, pageSize);
        postMapper.selectPage(postPage, new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getLikesCount, Post::getCreatedAt));

        List<ExploreItemVO> list = postPage.getRecords().stream().map(post -> {
            ExploreItemVO vo = new ExploreItemVO();
            vo.setPostId(String.valueOf(post.getId()));
            vo.setMediaType(post.getMediaType());
            vo.setCoverUrl(post.getCoverUrl());
            vo.setMediaCount(post.getMediaCount());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(list, postPage.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<SearchUserVO> searchUser(String keyword, Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();

        Page<User> userPage = new Page<>(page, pageSize);
        userMapper.selectPage(userPage, new LambdaQueryWrapper<User>()
                .like(User::getUsername, keyword)
                .or().like(User::getDisplayName, keyword)
                .orderByDesc(User::getFollowersCount));

        List<SearchUserVO> list = userPage.getRecords().stream().map(user -> {
            SearchUserVO vo = new SearchUserVO();
            vo.setUserId(String.valueOf(user.getId()));
            vo.setUsername(user.getUsername());
            vo.setDisplayName(user.getDisplayName());
            vo.setAvatar(user.getAvatar());
            vo.setIsVerified(user.getIsVerified() != null && user.getIsVerified() == 1);
            vo.setFollowersCount(user.getFollowersCount());
            vo.setIsFollowing(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getFollowerId, currentUserId)
                    .eq(Follow::getFollowingId, user.getId())) > 0);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(list, userPage.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<ExploreItemVO> searchPost(String keyword, String type, Integer page, Integer pageSize) {
        Page<Post> postPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .like(Post::getContent, keyword)
                .orderByDesc(Post::getCreatedAt);

        postMapper.selectPage(postPage, wrapper);

        List<ExploreItemVO> list = postPage.getRecords().stream().map(post -> {
            ExploreItemVO vo = new ExploreItemVO();
            vo.setPostId(String.valueOf(post.getId()));
            vo.setMediaType(post.getMediaType());
            vo.setCoverUrl(post.getCoverUrl());
            vo.setMediaCount(post.getMediaCount());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(list, postPage.getTotal(), page, pageSize);
    }
}
