package com.campus.index;

import com.campus.model.LearningResource;

import java.util.*;

public class InvertedIndex {
    // 倒排索引结构：Key = 关键词，Value = 包含该关键词的资料 ID 集合
    private Map<String, Set<Integer>> invertedIndex = new HashMap<>();

    //为单篇学习资料构建倒排索引
    public void addResource(LearningResource resource){
        Set<String> keywords = new HashSet<>();

        // 1. 提取标题中的关键词（按空格或标点切分，这里简化为按空格切分，或者将标签也加入索引）
        if(resource.getTitle() !=null){
            String[] titleWords = resource.getTitle().split("\\s+");
            Collections.addAll(keywords,titleWords);
        }

        // 2. 将资料自带的标签也作为索引关键词
        if(resource.getTags() !=null){
            keywords.addAll(resource.getTags());
        }
        // 3. 将关键词写入倒排表
        for(String word:keywords)
            invertedIndex.computeIfAbsent(word,k->new HashSet<>()).add(resource.getId());
    }

    public void removeResouce(LearningResource resource){
        if(resource == null){
            return ;
        }
        int targetId =  resource.getId();
        for( Map.Entry<String ,Set<Integer>> entry: invertedIndex.entrySet()){
            Set<Integer> idSet =  entry.getValue();
            if(idSet !=null){
                idSet.remove(targetId);
            }
        }
    }

    //根据关键词检索匹配的资料 ID 集合
    public Set<Integer> search(String keyword){
        // 如果关键词存在，返回对应的 ID 集合；如果不存在，返回空的 Set
        return  invertedIndex.getOrDefault(keyword,Collections.emptySet());
    }
    //清空或重新构建索引
    public void clear(){
        invertedIndex.clear();
    }

}

