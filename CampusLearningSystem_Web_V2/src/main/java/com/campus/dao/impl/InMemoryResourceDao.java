package com.campus.dao.impl;

import com.campus.dao.ResourceDao;
import com.campus.model.LearningResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryResourceDao implements ResourceDao {
    // 用 HashMap 在内存中模拟资料表：Key 是资料 ID，Value 是资料对象
    private Map<Integer,LearningResource> resourceTable = new HashMap<>();
    @Override
    public void addResource(LearningResource resource) {
        resourceTable.put(resource.getId(),resource);
    }

    @Override
    public LearningResource getResourceByID(int id) {
        return resourceTable.get(id) ;
    }

    @Override
    public List<LearningResource> getAllResources() {
        return new ArrayList<>(resourceTable.values());
    }

    @Override
    public void UpdateResource(LearningResource resource) {
        if(resourceTable.containsKey(resource.getId())){
            resourceTable.put(resource.getId(),resource) ;
        }
    }

    @Override
    public void deleteResource(int id) {
        resourceTable.remove(id);

    }
}
