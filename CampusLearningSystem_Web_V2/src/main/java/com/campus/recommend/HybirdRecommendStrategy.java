package com.campus.recommend;

import com.campus.model.LearningResource;
import com.campus.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HybirdRecommendStrategy implements RecommendStrategy{
    private final RecommendStrategy tagStrategy;
    private final RecommendStrategy cfStrategy;
    private final RecommendStrategy hotStrategy;

    public HybirdRecommendStrategy(RecommendStrategy tagStrategy, RecommendStrategy cfStrategy, RecommendStrategy hotStrategy) {
        this.tagStrategy = tagStrategy;
        this.cfStrategy = cfStrategy;
        this.hotStrategy = hotStrategy;
    }

    @Override
    public Map<Integer, Double> scoreAll(User user, Set<String> userTags, List<LearningResource> allResources) {
        Map<Integer,Double> result = new LinkedHashMap<>();
        if(allResources == null || allResources.isEmpty()) return result;

//        分别获取三种推荐算法的评分
        Map<Integer, Double> tagScores = tagStrategy.scoreAll(user,userTags,allResources);
        Map<Integer, Double> cfScores = cfStrategy.scoreAll(user,userTags,allResources);
        Map<Integer, Double> hotScores = hotStrategy.scoreAll(user,userTags,allResources);

//        对每个资源计算综合评分，权重分别给 0.4，0.4，0.2
        for(LearningResource resource: allResources){
            int id = resource.getId();
            double tag = tagScores.getOrDefault(id,0.0);
            double cf = cfScores.getOrDefault(id,0.0);
            double hot = hotScores.getOrDefault(id,0.0);
            double score =  tag*0.4 + cf*0.4 + hot*0.2;
            result.put(id,score);
        }
        return result;
    }
}
