package com.example.instagram.module.explore.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.explore.vo.ExploreItemVO;
import com.example.instagram.module.explore.vo.SearchTagVO;
import com.example.instagram.module.explore.vo.SearchUserVO;

public interface ExploreService {

    PageResult<ExploreItemVO> pageExploreFeed(Integer page, Integer pageSize);

    PageResult<SearchUserVO> searchUser(String keyword, Integer page, Integer pageSize);

    PageResult<SearchTagVO> searchTag(String keyword, Integer page, Integer pageSize);

    PageResult<ExploreItemVO> searchPost(String keyword, String type, Integer page, Integer pageSize);
}
