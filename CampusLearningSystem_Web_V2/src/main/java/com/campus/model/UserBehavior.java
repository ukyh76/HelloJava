package com.campus.model;

import com.campus.behavior.BehaviorType;

import java.time.LocalDateTime;
import java.util.Objects;

public record UserBehavior(int userID, int resourceID, BehaviorType type, LocalDateTime occurredAT) {
    //Compact Constructor
    public UserBehavior{
        Objects.requireNonNull(type);
        Objects.requireNonNull(occurredAT);

        if(userID <=0)
            throw new IllegalArgumentException("用户ID号必须是正数!");

        if (resourceID <= 0) {
            throw new IllegalArgumentException("学习资料ID号必须是正数!");
        }
    }


}
