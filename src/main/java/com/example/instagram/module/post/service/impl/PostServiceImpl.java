package com.example.instagram.module.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.post.dto.PostIdDTO;
import com.example.instagram.module.post.entity.Post;
import com.example.instagram.module.post.entity.PostLike;
import com.example.instagram.module.post.entity.PostMedia;
import com.example.instagram.module.post.entity.PostSave;
import com.example.instagram.module.post.mapper.PostLikeMapper;
import com.example.instagram.module.post.mapper.PostMapper;
import com.example.instagram.module.post.mapper.PostMediaMapper;
import com.example.instagram.module.post.mapper.PostSaveMapper;
import com.example.instagram.module.post.service.PostService;
import com.example.instagram.module.post.vo.MediaItemVO;
import com.example.instagram.module.post.vo.PostFeedVO;
import com.example.instagram.module.post.vo.PostLikeVO;
import com.example.instagram.module.post.vo.PostSaveVO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostMediaMapper postMediaMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostSaveMapper postSaveMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Override
    public PageResult<PostFeedVO> pagePostFeed(Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();

        // 查询帖子（简单实现：按时间倒序）
        Page<Post> postPage = new Page<>(page, pageSize);
        postMapper.selectPage(postPage, new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt));

        List<PostFeedVO> list = postPage.getRecords().stream()
                .map(post -> buildPostFeedVO(post, currentUserId))
                .collect(Collectors.toList());

        return PageResult.of(list, postPage.getTotal(), page, pageSize);
    }

    @Override
    public PostFeedVO getPostDetail(Long postId) {
        Long currentUserId = UserContext.getCurrentUserId();
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        return buildPostFeedVO(post, currentUserId);
    }

    @Override
    @Transactional
    public PostLikeVO likePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long postId = dto.getPostId();

        // 检查是否已点赞
        Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, currentUserId));
        if (count > 0) {
            throw BusinessException.conflict("已点赞");
        }

        // 插入点赞记录
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(currentUserId);
        postLikeMapper.insert(postLike);

        // 更新帖子点赞数
        Post post = postMapper.selectById(postId);
        post.setLikesCount(post.getLikesCount() + 1);
        postMapper.updateById(post);

        PostLikeVO vo = new PostLikeVO();
        vo.setIsLiked(true);
        vo.setLikesCount(post.getLikesCount());
        return vo;
    }

    @Override
    @Transactional
    public PostLikeVO unlikePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long postId = dto.getPostId();

        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, currentUserId));

        // 更新帖子点赞数
        Post post = postMapper.selectById(postId);
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        postMapper.updateById(post);

        PostLikeVO vo = new PostLikeVO();
        vo.setIsLiked(false);
        vo.setLikesCount(post.getLikesCount());
        return vo;
    }

    @Override
    @Transactional
    public PostSaveVO savePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long postId = dto.getPostId();

        Long count = postSaveMapper.selectCount(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, postId)
                .eq(PostSave::getUserId, currentUserId));
        if (count > 0) {
            throw BusinessException.conflict("已收藏");
        }

        PostSave postSave = new PostSave();
        postSave.setPostId(postId);
        postSave.setUserId(currentUserId);
        postSaveMapper.insert(postSave);

        PostSaveVO vo = new PostSaveVO();
        vo.setIsSaved(true);
        return vo;
    }

    @Override
    @Transactional
    public PostSaveVO unsavePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long postId = dto.getPostId();

        postSaveMapper.delete(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, postId)
                .eq(PostSave::getUserId, currentUserId));

        PostSaveVO vo = new PostSaveVO();
        vo.setIsSaved(false);
        return vo;
    }

    private PostFeedVO buildPostFeedVO(Post post, Long currentUserId) {
        User user = userMapper.selectById(post.getUserId());

        PostFeedVO vo = new PostFeedVO();
        vo.setPostId(String.valueOf(post.getId()));
        vo.setUserId(String.valueOf(post.getUserId()));
        vo.setUsername(user != null ? user.getUsername() : "");
        vo.setAvatar(user != null ? user.getAvatar() : "");
        vo.setIsVerified(user != null && user.getIsVerified() != null && user.getIsVerified() == 1);
        vo.setLocation(post.getLocation());
        vo.setContent(post.getContent());
        vo.setMediaType(post.getMediaType());
        vo.setMediaCount(post.getMediaCount());
        vo.setLikesCount(post.getLikesCount());
        vo.setCommentsCount(post.getCommentsCount());
        vo.setSharesCount(post.getSharesCount());

        // 查询媒体列表
        List<PostMedia> mediaList = postMediaMapper.selectList(new LambdaQueryWrapper<PostMedia>()
                .eq(PostMedia::getPostId, post.getId())
                .orderByAsc(PostMedia::getSortOrder));
        vo.setMediaList(mediaList.stream().map(m -> {
            MediaItemVO item = new MediaItemVO();
            item.setUrl(m.getMediaUrl());
            item.setType(m.getMediaType());
            return item;
        }).collect(Collectors.toList()));

        // 查询当前用户互动状态
        vo.setIsLiked(postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, post.getId())
                .eq(PostLike::getUserId, currentUserId)) > 0);

        vo.setIsSaved(postSaveMapper.selectCount(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, post.getId())
                .eq(PostSave::getUserId, currentUserId)) > 0);

        vo.setIsFollowing(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId)
                .eq(Follow::getFollowingId, post.getUserId())) > 0);

        vo.setCreatedAt(post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return vo;
    }
}
