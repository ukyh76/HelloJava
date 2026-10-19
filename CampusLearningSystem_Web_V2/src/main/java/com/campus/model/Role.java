package com.campus.model;

public enum Role {
    STUDENT("学生"),
    TEACHER("老师/管理员");
    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
