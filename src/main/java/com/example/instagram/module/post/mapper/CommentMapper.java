package com.example.instagram.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.instagram.module.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
