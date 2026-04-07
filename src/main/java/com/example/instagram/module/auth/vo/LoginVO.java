package com.example.instagram.module.auth.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private String refreshToken;
    private Long expiresIn;
}
