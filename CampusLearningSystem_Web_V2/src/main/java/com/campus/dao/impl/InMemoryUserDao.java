package com.campus.dao.impl;

import com.campus.dao.UserDao;
import com.campus.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryUserDao implements UserDao {
    HashMap<String, User> userTable = new HashMap<>();

    @Override
    public void addUser(User user) {
        userTable.put(user.getUsername(),user);


    }

    @Override
    public User getUserByUsername(String username) {
        return userTable.get(username);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userTable.values());
    }
}
