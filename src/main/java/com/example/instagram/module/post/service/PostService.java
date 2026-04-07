package com.example.instagram.module.post.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.post.dto.PostIdDTO;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.post.vo.PostLikeVO;
import com.example.instagram.module.post.vo.PostSaveVO;

public interface PostService {

    PageResult<PostFeedVO> pagePostFeed(Integer page, Integer pageSize);

    PostFeedVO getPostDetail(Long postId);

    PostLikeVO likePost(PostIdDTO dto);

    PostLikeVO unlikePost(PostIdDTO dto);

    PostSaveVO savePost(PostIdDTO dto);

    PostSaveVO unsavePost(PostIdDTO dto);
}
