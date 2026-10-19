package com.campus.recommend;

import com.campus.model.LearningResource;
import com.campus.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RecommendStrategy {
    Map<Integer, Double> scoreAll(
            User user,
            Set<String> userTags,
            List<LearningResource> allResources
    );
}
