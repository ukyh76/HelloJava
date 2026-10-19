package com.campus.service;

import com.campus.Repository.BehaviorRepository;
import com.campus.behavior.BehaviorType;
import com.campus.model.User;
import com.campus.model.UserBehavior;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public final  class BehaviorService {

    //一个集合 + record() + getBehaviorsByUser()。
    private final BehaviorRepository behaviors;

    public BehaviorService(BehaviorRepository behaviors) {
        this.behaviors = behaviors;
    }
    public void record(int userID, int resourceID, BehaviorType type){
          behaviors.save( new UserBehavior(userID,resourceID,type, LocalDateTime.now())) ;
    }

    public Set<Integer>  consumedResoucedIds(int userID){
        return behaviors.findByUserID(userID).stream().map(UserBehavior::resourceID).collect(Collectors.toSet());

    }
}


