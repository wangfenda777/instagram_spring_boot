package com.example.instagram.module.tag.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.tag.vo.TagVO;

import java.util.List;
import java.util.Map;

public interface TagService {

    void syncPostTags(Long postId, String content);

    void removePostTags(Long postId);

    Map<Long, List<TagVO>> getTagsByPostIds(List<Long> postIds);

    PageResult<TagVO> searchTags(String keyword, Integer page, Integer pageSize);

    void recordTagSearch(Long userId, String keyword);

    List<Long> findPostIdsByTagKeyword(String keyword);
}
