package com.campus.Repository;

import com.campus.model.UserBehavior;

import java.util.List;

public interface BehaviorRepository {
    void save(UserBehavior behavior);
    List<UserBehavior> findAll();
    List<UserBehavior> findByUserID(int userID);



}
