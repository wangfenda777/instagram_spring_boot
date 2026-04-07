package com.example.instagram.module.user.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.user.dto.UpdateProfileDTO;
import com.example.instagram.module.user.vo.*;

import java.util.List;

public interface UserService {

    UserInfoVO getCurrentUser();

    UserInfoVO getUserInfo(Long userId);

    UserStatsVO getUserStats(Long userId);

    UserInfoVO updateProfile(UpdateProfileDTO dto);

    PageResult<UserPostGridVO> listUserPosts(Long userId, Integer page, Integer pageSize);

    PageResult<UserReelVO> listUserReels(Long userId, Integer page, Integer pageSize);

    List<DiscoverUserVO> listDiscoverUsers(Integer limit);
}
