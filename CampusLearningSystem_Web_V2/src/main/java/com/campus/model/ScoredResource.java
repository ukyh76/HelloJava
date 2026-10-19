package com.campus.model;

public class ScoredResource implements  Comparable<ScoredResource>{
    private LearningResource resource;
    private double score;

    public ScoredResource(LearningResource resource, double score) {
        this.resource = resource;
        this.score = score;
    }

    public LearningResource getResource(){
        return  resource;
    }

    public double getScore(){
        return score;
    }


    @Override
    public int compareTo(ScoredResource o) {
        return Double.compare(o.score,this.score);
    }
}
