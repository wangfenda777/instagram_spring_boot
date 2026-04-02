package com.example.usermanage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("用户管理系统API")
                        .version("1.0.0")
                        .description("用户增删改查接口文档")
                        .contact(new Contact()
                                .name("启哥")
                                .email("example@example.com")));
    }
}