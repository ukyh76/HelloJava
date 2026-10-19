package com.campus.dao;

import com.campus.model.User;

import java.util.List;

public interface UserDao {
    void addUser(User user);
    User getUserByUsername(String username);
    List<User> getAllUsers();
}
