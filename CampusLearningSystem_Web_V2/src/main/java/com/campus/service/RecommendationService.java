package com.campus.service;

import com.campus.model.LearningResource;

import java.util.*;

public class RecommendationService {
    // 计算 Jaccard 相似度
    public double calculateJaccard(Set<String> userTags,Set<String> resourceTags){
        if(userTags == null || resourceTags == null || userTags.isEmpty() || resourceTags.isEmpty())
            return 0;
        Set<String> intersection  =new HashSet<>(userTags);//        Set<String> intersection  =userTags;//不推荐，userTags和intersection相互影响，因为传递了引用
        intersection.retainAll(resourceTags);

        Set<String> union = new HashSet<>(userTags);
        union.addAll(resourceTags);

        return  (double) (intersection.size() *1.0/ union.size());
    }

    //利用PriorityQueue (最大堆）获取Top-N 推荐结果
    public List<LearningResource> getTopRecommendations(Set<String> userTags, List<LearningResource> allResources,int n){
        PriorityQueue<Map.Entry<LearningResource,Double>> maxHeap = new PriorityQueue<>((e1,e2)->Double.compare(e2.getValue(),e1.getValue()));
        for(LearningResource resource:allResources){
            double score = calculateJaccard(userTags,resource.getTags());
            maxHeap.offer(new AbstractMap.SimpleEntry<>(resource,score));//offer方法需要一个实现了Map.Entry的接口的实现类的对象。AbstractMap.SimpleEntry 就是 Java 提供的一个最简单的实现，
        }
        List<LearningResource> topNList = new ArrayList<>();
        for (int i = 0; i < n && !maxHeap.isEmpty(); i++) {
            topNList.add(maxHeap.poll().getKey());
        }
        return topNList;
    }

}
