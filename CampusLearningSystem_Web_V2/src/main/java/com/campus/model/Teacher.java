package com.campus.model;

public class Teacher extends User {

    public Teacher(String username, String password) {
        super(1002,username, password, Role.TEACHER);
    }

    @Override
    public void showDashboard() {
        System.out.println("=== 教师/管理员端主菜单 ===");
        System.out.println("1. 学习资料增删改查 (CRUD)");
        System.out.println("2. 查看所有资料与倒排索引状态");
        System.out.println("0. 退出登录");

    }
}
