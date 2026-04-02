package com.example.usermanage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usermanage.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User getUserById(Long id);

    List<User> getAllUsers();

    boolean createUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(Long id);
}