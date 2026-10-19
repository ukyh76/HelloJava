package com.campus.recommend;

import com.campus.model.LearningResource;
import com.campus.model.User;
import com.campus.service.RecommendationService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


//标签推荐策略

public class TagSimilarityStrategy implements RecommendStrategy {
    private final RecommendationService recommendationService;

    public TagSimilarityStrategy() {
        this(new RecommendationService());
    }

    public TagSimilarityStrategy(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    @Override
    public Map<Integer, Double> scoreAll(User user, Set<String> userTags, List<LearningResource> allResources) {
        Map<Integer,Double> scores = new LinkedHashMap<>();
        if(allResources == null) return scores;
        for(LearningResource resource:allResources){
            double score = recommendationService.calculateJaccard(userTags,resource.getTags());
            scores.put(resource.getId(),score);
        }
        return scores;
    }

}
