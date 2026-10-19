package com.campus.dao;

import com.campus.model.LearningResource;

import java.util.List;

public interface ResourceDao
{
    void addResource(LearningResource resource);
    LearningResource getResourceByID(int id);
    List<LearningResource> getAllResources();
    void UpdateResource(LearningResource resource);
    void deleteResource(int id);

}
