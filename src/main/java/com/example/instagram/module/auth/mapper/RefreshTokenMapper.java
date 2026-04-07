package com.example.instagram.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.instagram.module.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
