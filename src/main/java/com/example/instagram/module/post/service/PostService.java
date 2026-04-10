package com.example.instagram.module.post.service;

import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.post.dto.PostCreateDTO;
import com.example.instagram.module.post.dto.PostIdDTO;
import com.example.instagram.module.post.dto.PostUpdateDTO;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.post.vo.PostLikeVO;
import com.example.instagram.module.post.vo.PostSaveVO;

public interface PostService {

    PostFeedVO createPost(PostCreateDTO dto);

    void updatePost(PostUpdateDTO dto);

    void deletePost(PostIdDTO dto);

    PageResult<PostFeedVO> pagePostFeed(Long lastId, Integer pageSize);

    PostFeedVO getPostDetail(Long postId);

    PostLikeVO likePost(PostIdDTO dto);

    PostLikeVO unlikePost(PostIdDTO dto);

    PostSaveVO savePost(PostIdDTO dto);

    PostSaveVO unsavePost(PostIdDTO dto);
}
