package com.example.instagram.module.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.common.utils.UserContext;
import com.example.instagram.module.follow.entity.Follow;
import com.example.instagram.module.follow.mapper.FollowMapper;
import com.example.instagram.module.post.dto.PostCreateDTO;
import com.example.instagram.module.post.dto.PostIdDTO;
import com.example.instagram.module.post.dto.PostUpdateDTO;
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
import com.example.instagram.module.tag.service.TagService;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    @Autowired
    private TagService tagService;

    @Override
    @Transactional
    public PostFeedVO createPost(PostCreateDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();

        Post post = new Post();
        post.setUserId(currentUserId);
        post.setContent(dto.getContent());
        post.setLocation(dto.getLocation());
        post.setMediaType(dto.getMediaType());
        post.setMediaCount(dto.getMediaUrls().size());
        post.setCoverUrl(dto.getMediaUrls().get(0));
        post.setLikesCount(0);
        post.setSavedCount(0);
        post.setCommentsCount(0);
        post.setSharesCount(0);
        post.setViewsCount(0);
        post.setIsDeleted(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.insert(post);

        // 重新查询获取数据库生成的时间戳
        post = postMapper.selectById(post.getId());

        for (int i = 0; i < dto.getMediaUrls().size(); i++) {
            PostMedia media = new PostMedia();
            media.setPostId(post.getId());
            media.setMediaUrl(dto.getMediaUrls().get(i));
            media.setMediaType(dto.getMediaType());
            media.setSortOrder(i);
            media.setCreatedAt(LocalDateTime.now());
            postMediaMapper.insert(media);
        }

        tagService.syncPostTags(post.getId(), post.getContent());

        return buildPostFeedVO(post, currentUserId);
    }

    @Override
    @Transactional
    public void updatePost(PostUpdateDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!post.getUserId().equals(currentUserId)) {
            throw BusinessException.badRequest("只能编辑自己的帖子");
        }
        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        if (dto.getLocation() != null) {
            post.setLocation(dto.getLocation());
        }
        postMapper.updateById(post);
        tagService.syncPostTags(post.getId(), post.getContent());
    }

    @Override
    @Transactional
    public void deletePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!post.getUserId().equals(currentUserId)) {
            throw BusinessException.badRequest("只能删除自己的帖子");
        }
        tagService.removePostTags(dto.getPostId());
        postMapper.deleteById(dto.getPostId());
    }

    @Override
    public PageResult<PostFeedVO> pagePostFeed(Long lastId, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();
        int finalPageSize = pageSize == null || pageSize <= 0 ? 6 : pageSize;
        int candidateLimit = Math.max(15, finalPageSize * 2 + 3);

        Set<Long> followUserIds = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, currentUserId))
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> likedPostIds = postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, currentUserId))
                .stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toSet());

        Set<Long> savedPostIds = postSaveMapper.selectList(new LambdaQueryWrapper<PostSave>()
                        .eq(PostSave::getUserId, currentUserId))
                .stream()
                .map(PostSave::getPostId)
                .collect(Collectors.toSet());

        Set<Long> excludedPostIds = new HashSet<>();
        excludedPostIds.addAll(likedPostIds);
        excludedPostIds.addAll(savedPostIds);

        LambdaQueryWrapper<Post> candidateWrapper = new LambdaQueryWrapper<Post>()
                .ne(Post::getUserId, currentUserId)
                .orderByDesc(Post::getId)
                .last("LIMIT " + candidateLimit);
        if (lastId != null && lastId > 0) {
            candidateWrapper.lt(Post::getId, lastId);
        }

        List<Post> candidatePosts = postMapper.selectList(candidateWrapper);

        List<Post> followPool = candidatePosts.stream()
                .filter(post -> followUserIds.contains(post.getUserId()))
                .filter(post -> !excludedPostIds.contains(post.getId()))
                .collect(Collectors.toList());
        List<Post> recommendPool = candidatePosts.stream()
                .filter(post -> !followUserIds.contains(post.getUserId()))
                .filter(post -> !excludedPostIds.contains(post.getId()))
                .collect(Collectors.toList());

        int followTarget = Math.min(4, finalPageSize);
        int recommendTarget = Math.max(0, finalPageSize - followTarget);

        List<Post> selectedFollowPosts = selectTopPosts(followPool, followTarget, true);
        Set<Long> selectedIds = selectedFollowPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (selectedFollowPosts.size() < followTarget && !followUserIds.isEmpty()) {
            List<Post> historyFollowPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .in(Post::getUserId, followUserIds)
                    .ne(Post::getUserId, currentUserId)
                    .orderByDesc(Post::getId));
            for (Post post : historyFollowPosts) {
                if (selectedFollowPosts.size() >= followTarget) {
                    break;
                }
                if (excludedPostIds.contains(post.getId()) || selectedIds.contains(post.getId())) {
                    continue;
                }
                selectedFollowPosts.add(post);
                selectedIds.add(post.getId());
            }
        }

        List<Post> selectedRecommendPosts = selectTopPosts(
                recommendPool.stream()
                        .filter(post -> !selectedIds.contains(post.getId()))
                        .collect(Collectors.toList()),
                recommendTarget,
                false);
        selectedRecommendPosts.forEach(post -> selectedIds.add(post.getId()));

        if (selectedRecommendPosts.size() < recommendTarget) {
            List<Post> hotPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .ne(Post::getUserId, currentUserId)
                    .orderByDesc(Post::getLikesCount)
                    .orderByDesc(Post::getSavedCount)
                    .orderByDesc(Post::getCommentsCount)
                    .orderByDesc(Post::getSharesCount)
                    .orderByDesc(Post::getId));
            for (Post post : hotPosts) {
                if (selectedRecommendPosts.size() >= recommendTarget) {
                    break;
                }
                if (excludedPostIds.contains(post.getId()) || selectedIds.contains(post.getId())) {
                    continue;
                }
                selectedRecommendPosts.add(post);
                selectedIds.add(post.getId());
            }
        }

        List<Post> finalPosts = new ArrayList<>();
        finalPosts.addAll(selectedFollowPosts);
        finalPosts.addAll(selectedRecommendPosts);
        finalPosts.sort(Comparator.comparing(Post::getId).reversed());

        List<PostFeedVO> list = finalPosts.stream()
                .limit(finalPageSize)
                .map(post -> buildPostFeedVO(post, currentUserId))
                .collect(Collectors.toList());

        PageResult<PostFeedVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal((long) list.size());
        result.setPage(1);
        result.setPageSize(finalPageSize);
        result.setHasMore(candidatePosts.size() == candidateLimit);
        result.setLastId(finalPosts.stream().map(Post::getId).min(Long::compareTo).orElse(0L));
        return result;
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

        Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, currentUserId));
        if (count > 0) {
            throw BusinessException.conflict("已点赞");
        }

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(currentUserId);
        postLikeMapper.insert(postLike);

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

        Post post = postMapper.selectById(postId);
        post.setSavedCount((post.getSavedCount() == null ? 0 : post.getSavedCount()) + 1);
        postMapper.updateById(post);

        PostSaveVO vo = new PostSaveVO();
        vo.setIsSaved(true);
        vo.setSavedCount(post.getSavedCount());
        return vo;
    }

    @Override
    @Transactional
    public PostSaveVO unsavePost(PostIdDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long postId = dto.getPostId();

        Long count = postSaveMapper.selectCount(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, postId)
                .eq(PostSave::getUserId, currentUserId));
        if (count == 0) {
            throw BusinessException.conflict("未收藏该帖子");
        }

        postSaveMapper.delete(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, postId)
                .eq(PostSave::getUserId, currentUserId));

        Post post = postMapper.selectById(postId);
        post.setSavedCount(Math.max(0, (post.getSavedCount() == null ? 0 : post.getSavedCount()) - 1));
        postMapper.updateById(post);

        PostSaveVO vo = new PostSaveVO();
        vo.setIsSaved(false);
        vo.setSavedCount(post.getSavedCount());
        return vo;
    }

    private List<Post> selectTopPosts(List<Post> posts, int limit, boolean following) {
        Comparator<Post> comparator = Comparator
                .comparingLong((Post post) -> calculateScore(post, following))
                .reversed()
                .thenComparing(Post::getId, Comparator.reverseOrder());
        return posts.stream()
                .sorted(comparator)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private long calculateScore(Post post, boolean following) {
        long likes = post.getLikesCount() == null ? 0 : post.getLikesCount();
        long saves = post.getSavedCount() == null ? 0 : post.getSavedCount();
        long comments = post.getCommentsCount() == null ? 0 : post.getCommentsCount();
        long shares = post.getSharesCount() == null ? 0 : post.getSharesCount();
        long views = post.getViewsCount() == null ? 0 : post.getViewsCount();
        long baseScore = likes + comments * 3 + saves * 4 + shares * 5 + views / 10;
        return following ? baseScore + post.getId() : baseScore * 2 + post.getId();
    }

    @Override
    public PostFeedVO buildPostFeedVO(Post post, Long currentUserId) {
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
        vo.setSavedCount(post.getSavedCount());
        vo.setCommentsCount(post.getCommentsCount());
        vo.setSharesCount(post.getSharesCount());

        List<PostMedia> mediaList = postMediaMapper.selectList(new LambdaQueryWrapper<PostMedia>()
                .eq(PostMedia::getPostId, post.getId())
                .orderByAsc(PostMedia::getSortOrder));
        vo.setMediaList(mediaList.stream().map(m -> {
            MediaItemVO item = new MediaItemVO();
            item.setUrl(m.getMediaUrl());
            item.setType(m.getMediaType());
            return item;
        }).collect(Collectors.toList()));

        vo.setIsLiked(postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, post.getId())
                .eq(PostLike::getUserId, currentUserId)) > 0);

        vo.setIsSaved(postSaveMapper.selectCount(new LambdaQueryWrapper<PostSave>()
                .eq(PostSave::getPostId, post.getId())
                .eq(PostSave::getUserId, currentUserId)) > 0);

        vo.setIsFollowing(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, currentUserId)
                .eq(Follow::getFollowingId, post.getUserId())) > 0);
        vo.setTags(tagService.getTagsByPostIds(List.of(post.getId())).getOrDefault(post.getId(), List.of()));

        vo.setCreatedAt(post.getCreatedAt() != null
                ? post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis());
        return vo;
    }
}
