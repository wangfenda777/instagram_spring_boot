package com.example.instagram.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.instagram.common.exception.BusinessException;
import com.example.instagram.common.utils.JwtUtil;
import com.example.instagram.common.utils.PasswordUtil;
import com.example.instagram.module.auth.dto.LoginDTO;
import com.example.instagram.module.auth.dto.RefreshTokenDTO;
import com.example.instagram.module.auth.entity.RefreshToken;
import com.example.instagram.module.auth.mapper.RefreshTokenMapper;
import com.example.instagram.module.auth.service.AuthService;
import com.example.instagram.module.auth.vo.LoginVO;
import com.example.instagram.module.user.entity.User;
import com.example.instagram.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Override
    @Transactional
    public LoginVO login(LoginDTO dto) {
        // 支持用户名/邮箱/手机号登录
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getAccount())
                .or().eq(User::getEmail, dto.getAccount())
                .or().eq(User::getPhone, dto.getAccount()));

        if (user == null) {
            throw BusinessException.badRequest("账号不存在");
        }

        if (!dto.getPassword().equals(user.getPassword())) {
            throw BusinessException.badRequest("密码错误");
        }

        return generateTokens(user.getId());
    }

    @Override
    @Transactional
    public LoginVO refreshToken(RefreshTokenDTO dto) {
        // 验证 refreshToken
        if (!JwtUtil.validateToken(dto.getRefreshToken())) {
            throw BusinessException.unauthorized("refreshToken 无效或已过期");
        }

        // 检查是否已撤销
        RefreshToken storedToken = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getToken, dto.getRefreshToken())
                .eq(RefreshToken::getIsRevoked, 0));

        if (storedToken == null) {
            throw BusinessException.unauthorized("refreshToken 无效或已撤销");
        }

        // 撤销旧 token
        storedToken.setIsRevoked(1);
        refreshTokenMapper.updateById(storedToken);

        Long userId = JwtUtil.getUserIdFromToken(dto.getRefreshToken());
        return generateTokens(userId);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        // 撤销该用户所有未撤销的 refreshToken
        RefreshToken update = new RefreshToken();
        update.setIsRevoked(1);
        refreshTokenMapper.update(update, new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getUserId, userId)
                .eq(RefreshToken::getIsRevoked, 0));
    }

    private LoginVO generateTokens(Long userId) {
        String accessToken = JwtUtil.generateAccessToken(userId);
        String refreshTokenStr = JwtUtil.generateRefreshToken(userId);

        // 保存 refreshToken 到数据库
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setExpiredAt(LocalDateTime.ofInstant(
                JwtUtil.getRefreshTokenExpireDate().toInstant(), ZoneId.systemDefault()));
        refreshToken.setIsRevoked(0);
        refreshTokenMapper.insert(refreshToken);

        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setRefreshToken(refreshTokenStr);
        vo.setExpiresIn(JwtUtil.getAccessTokenExpireSeconds());
        return vo;
    }
}
