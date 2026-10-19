package com.campus.model;

import java.util.HashSet;
import java.util.Set;

public abstract class User {
    private int userID;
    private String username;
    private String password;
    private Role role;
    private Set<String> tags;
    private  String faceImage;

    public User(int userID,String username, String password, Role role) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.role = role;
        this.tags = new HashSet<>();
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public void addTag(String tag){
        this.tags.add(tag);
    }

    public void removeTag(String tag){
        this.tags.remove(tag);
    }

    public abstract void showDashboard();  //不同角色可能会展现不同的菜单，或者是权限显示。
}
