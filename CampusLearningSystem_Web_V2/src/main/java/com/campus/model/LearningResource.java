package com.campus.model;

import java.util.HashSet;
import java.util.Set;

public class LearningResource {
    private int id;
    private String title;
    private String category;
    private Set<String> tags;//标签
    private int heatScore; //热度

    public LearningResource(int id, String title, String category, Set<String> tags, int hearScore) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.tags = tags;
        this.heatScore = hearScore;
    }

    public LearningResource(int id, String title, String category, int heatScore) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.heatScore = heatScore;
        this.tags = new HashSet<>();
    }

    public     void addTag(String tag){
        this.tags.add(tag);
    }
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public Set<String> getTags() {
        return tags;
    }

    public int getHeatScore() {
        return heatScore;
    }

    @Override
    public String toString() {
        return "LearningResource{" +
                "ID=" + id +
                ", 标题='" + title + '\'' +
                ", 类别='" + category + '\'' +
                ", 标签=" + tags +
                ", 热度=" + heatScore +
                '}';
    }
}
