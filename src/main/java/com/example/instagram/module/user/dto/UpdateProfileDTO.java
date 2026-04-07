package com.example.instagram.module.user.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {

    private String displayName;
    private String bio;
    private String avatar;
}
