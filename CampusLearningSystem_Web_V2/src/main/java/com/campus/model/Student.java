package com.campus.model;

public class Student extends User{
    public Student(String username, String password){
        super(1001,username, password,Role.STUDENT);
    }
    @Override
    public void showDashboard() {
        System.out.println("=== 学生端主菜单 ===");
        System.out.println("1. 浏览与检索学习资料");
        System.out.println("2. 查看个性化推荐 Top-N");
        System.out.println("3. AI 助教智能问答");
        System.out.println("0. 退出登录");


    }
}
