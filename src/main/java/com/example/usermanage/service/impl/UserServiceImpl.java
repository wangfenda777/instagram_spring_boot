package com.example.usermanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usermanage.entity.User;
import com.example.usermanage.mapper.UserMapper;
import com.example.usermanage.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUserById(Long id) {
        return this.getById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return this.list();
    }

    @Override
    public boolean createUser(User user) {
        return this.save(user);
    }

    @Override
    public boolean updateUser(User user) {
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }
}