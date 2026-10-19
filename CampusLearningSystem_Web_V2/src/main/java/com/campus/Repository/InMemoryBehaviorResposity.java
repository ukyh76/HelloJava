package com.campus.Repository;

import com.campus.model.UserBehavior;

import java.util.ArrayList;
import java.util.List;

public class InMemoryBehaviorResposity implements BehaviorRepository{
    private final  List<UserBehavior> behaviors  = new ArrayList<>();

    @Override
    public void save(UserBehavior behavior) {
        behaviors.add(behavior);
    }

    @Override
    public List<UserBehavior> findAll() {
        return List.copyOf(behaviors); //或者new ArrayList<>(behaviors)
    }

    @Override
    public List<UserBehavior> findByUserID(int userID) {
        return behaviors.stream().filter(behavior->behavior.userID() == userID).toList();

    }
}
