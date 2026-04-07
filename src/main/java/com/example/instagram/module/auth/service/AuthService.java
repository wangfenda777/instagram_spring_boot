package com.example.instagram.module.auth.service;

import com.example.instagram.module.auth.dto.LoginDTO;
import com.example.instagram.module.auth.dto.RefreshTokenDTO;
import com.example.instagram.module.auth.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    LoginVO refreshToken(RefreshTokenDTO dto);

    void logout(Long userId);
}
