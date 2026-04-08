package com.example.instagram.module.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.instagram.common.result.PageResult;
import com.example.instagram.module.tag.entity.PostTag;
import com.example.instagram.module.tag.entity.Tag;
import com.example.instagram.module.tag.entity.TagSearchHistory;
import com.example.instagram.module.tag.mapper.PostTagMapper;
import com.example.instagram.module.tag.mapper.TagMapper;
import com.example.instagram.module.tag.mapper.TagSearchHistoryMapper;
import com.example.instagram.module.tag.service.TagService;
import com.example.instagram.module.tag.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private static final Pattern TAG_PATTERN = Pattern.compile("(?<![\\p{L}\\p{N}_])#([\\p{L}\\p{N}_\\u4e00-\\u9fa5]+)");

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private TagSearchHistoryMapper tagSearchHistoryMapper;

    @Override
    @Transactional
    public void syncPostTags(Long postId, String content) {
        List<PostTag> existingRelations = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));
        Set<Long> existingTagIds = existingRelations.stream()
                .map(PostTag::getTagId)
                .collect(Collectors.toSet());

        Set<String> tagNames = extractTagNames(content);
        if (tagNames.isEmpty()) {
            if (!existingRelations.isEmpty()) {
                postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
                decrementPostCount(existingTagIds);
            }
            return;
        }

        Map<String, Tag> tagMap = findOrCreateTags(tagNames);
        Set<Long> newTagIds = tagMap.values().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        Set<Long> toRemove = new LinkedHashSet<>(existingTagIds);
        toRemove.removeAll(newTagIds);
        if (!toRemove.isEmpty()) {
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>()
                    .eq(PostTag::getPostId, postId)
                    .in(PostTag::getTagId, toRemove));
            decrementPostCount(toRemove);
        }

        Set<Long> toAdd = new LinkedHashSet<>(newTagIds);
        toAdd.removeAll(existingTagIds);
        for (Long tagId : toAdd) {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            postTagMapper.insert(postTag);
        }
        incrementPostCount(toAdd);
    }

    @Override
    @Transactional
    public void removePostTags(Long postId) {
        List<PostTag> relations = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));
        if (relations.isEmpty()) {
            return;
        }
        Set<Long> tagIds = relations.stream().map(PostTag::getTagId).collect(Collectors.toSet());
        postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        decrementPostCount(tagIds);
    }

    @Override
    public Map<Long, List<TagVO>> getTagsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PostTag> relations = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .in(PostTag::getPostId, postIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = relations.stream().map(PostTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));

        Map<Long, List<TagVO>> result = new LinkedHashMap<>();
        for (PostTag relation : relations) {
            Tag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(relation.getPostId(), key -> new ArrayList<>())
                    .add(toTagVO(tag));
        }
        return result;
    }

    @Override
    public PageResult<TagVO> searchTags(String keyword, Integer page, Integer pageSize) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Tag> tagPage = new Page<>(page, pageSize);
        tagMapper.selectPage(tagPage, new LambdaQueryWrapper<Tag>()
                .like(Tag::getName, normalizedKeyword)
                .orderByDesc(Tag::getHeat, Tag::getPostCount, Tag::getUpdatedAt));

        List<TagVO> list = tagPage.getRecords().stream()
                .map(this::toTagVO)
                .collect(Collectors.toList());
        return PageResult.of(list, tagPage.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public void recordTagSearch(Long userId, String keyword) {
        if (userId == null) {
            return;
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return;
        }

        TagSearchHistory history = tagSearchHistoryMapper.selectOne(new LambdaQueryWrapper<TagSearchHistory>()
                .eq(TagSearchHistory::getUserId, userId)
                .eq(TagSearchHistory::getKeyword, normalizedKeyword));
        if (history == null) {
            history = new TagSearchHistory();
            history.setUserId(userId);
            history.setKeyword(normalizedKeyword);
            history.setSearchCount(1);
            tagSearchHistoryMapper.insert(history);
        } else {
            history.setSearchCount((history.getSearchCount() == null ? 0 : history.getSearchCount()) + 1);
            history.setUpdatedAt(LocalDateTime.now());
            tagSearchHistoryMapper.updateById(history);
        }

        List<Tag> matchedTags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, normalizedKeyword));
        for (Tag tag : matchedTags) {
            tag.setHeat((tag.getHeat() == null ? 0 : tag.getHeat()) + 1);
            tagMapper.updateById(tag);
        }
    }

    @Override
    public List<Long> findPostIdsByTagKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, normalizedKeyword)
                .orderByDesc(Tag::getHeat, Tag::getPostCount));
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
        return postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                        .in(PostTag::getTagId, tagIds))
                .stream()
                .map(PostTag::getPostId)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, Tag> findOrCreateTags(Set<String> tagNames) {
        List<Tag> existingTags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .in(Tag::getName, tagNames));
        Map<String, Tag> result = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        for (String tagName : tagNames) {
            if (result.containsKey(tagName)) {
                continue;
            }
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setHeat(0);
            tag.setPostCount(0);
            tagMapper.insert(tag);
            result.put(tagName, tag);
        }
        return result;
    }

    private Set<String> extractTagNames(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptySet();
        }
        Matcher matcher = TAG_PATTERN.matcher(content);
        Set<String> tags = new LinkedHashSet<>();
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }

    private void incrementPostCount(Set<Long> tagIds) {
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                continue;
            }
            tag.setPostCount((tag.getPostCount() == null ? 0 : tag.getPostCount()) + 1);
            tagMapper.updateById(tag);
        }
    }

    private void decrementPostCount(Set<Long> tagIds) {
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                continue;
            }
            int current = tag.getPostCount() == null ? 0 : tag.getPostCount();
            tag.setPostCount(Math.max(0, current - 1));
            tagMapper.updateById(tag);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String normalized = keyword.trim().toLowerCase();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private TagVO toTagVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setTagId(String.valueOf(tag.getId()));
        vo.setName("#" + tag.getName());
        vo.setHeat(tag.getHeat());
        vo.setPostCount(tag.getPostCount());
        return vo;
    }
}
