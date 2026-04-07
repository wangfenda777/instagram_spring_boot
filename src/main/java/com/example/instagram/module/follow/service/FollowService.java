package com.example.instagram.module.follow.service;

import com.example.instagram.module.follow.dto.FollowDTO;
import com.example.instagram.module.follow.vo.FollowVO;

public interface FollowService {

    FollowVO follow(FollowDTO dto);

    FollowVO unfollow(FollowDTO dto);
}
