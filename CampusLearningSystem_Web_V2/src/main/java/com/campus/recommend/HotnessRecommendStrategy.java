package com.campus.recommend;

import com.campus.model.LearningResource;
import com.campus.model.User;

import java.util.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
pblic class HotnessRecommendStrategy implements RecommendStrategy {
    @Override
    public List<LearningResource> recommend(Set<String> userTags, List<LearningResource> allResources, int n) {
        if(allResources == null || allResources.isEmpty()){
            return  new ArrayList<>();
        }
        List<LearningResource> sortedList = new ArrayList<>(allResources);
        sortedList.sort((r1,r2)->Integer.compare(r2.getHeatScore(),r1.getHeatScore()));

        return sortedList.subList(0,Math.min(n,sortedList.size()));
    }
}
*/


//热度推荐策略
public class HotnessRecommendStrategy implements RecommendStrategy {
    @Override
    public Map<Integer, Double> scoreAll(
            User user,
            Set<String> userTags,
            List<LearningResource> allResources
    ) {
        Map<Integer, Double> rawScores = new LinkedHashMap<>();
        if (allResources == null || allResources.isEmpty()) {
            return rawScores;
        }

        for (LearningResource resource : allResources) {
            rawScores.put(resource.getId(), (double) resource.getHeatScore());
        }
        return ScoreUtils.normalize(rawScores);
    }
}
