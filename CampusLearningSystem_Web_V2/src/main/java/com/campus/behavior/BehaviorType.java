package com.campus.behavior;

public enum BehaviorType {
    VIEW(1.0),LOOK(1),FAVORITE(3),DOWNLOAD(5);

    private final double weight;
    BehaviorType(double weight){
        this.weight  = weight;
    }

    public double getWeight() {
        return weight;
    }
}
