package com.example.instagram.module.follow.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.follow.dto.FollowDTO;
import com.example.instagram.module.follow.vo.FollowUserVO;
import com.example.instagram.module.follow.vo.FollowVO;

public interface FollowService {

    FollowVO follow(FollowDTO dto);

    FollowVO unfollow(FollowDTO dto);

    PageResult<FollowUserVO> pageFollowers(Long userId, Integer page, Integer pageSize);

    PageResult<FollowUserVO> pageFollowing(Long userId, Integer page, Integer pageSize);
}
