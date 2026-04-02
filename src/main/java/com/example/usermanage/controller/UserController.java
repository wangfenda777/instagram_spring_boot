package com.example.usermanage.controller;

import com.example.usermanage.entity.User;
import com.example.usermanage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户增删改查接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public User getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "查询所有用户")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public boolean createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    @Operation(summary = "更新用户")
    public boolean updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public boolean deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.deleteUser(id);
    }
}