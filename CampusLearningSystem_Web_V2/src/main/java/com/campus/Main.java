package com.campus;

import com.campus.model.LearningResource;
import com.campus.model.Student;
import com.campus.model.Teacher;
import com.campus.model.User;
import com.campus.service.ResourceService;

import java.util.Arrays;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {

        ResourceService resourceService = new ResourceService();
        // 初始化一些测试数据
        // 创建教师账号（addResource 要求操作人必须是 TEACHER 角色）
        User teacher = new Teacher("teacher_li", "123456");
        resourceService.addResource(teacher,new LearningResource(1, "Java面向对象基础笔记", "复习笔记", new HashSet<>(Arrays.asList("Java", "面向对象", "基础")), 85));
        resourceService.addResource(teacher,new LearningResource(2, "数据结构排序算法题解", "算法题解", new HashSet<>(Arrays.asList("DataStructure", "排序", "算法")), 92));
        resourceService.addResource(teacher,new LearningResource(3, "Spring Boot开源项目源码", "开源项目源码", new HashSet<>(Arrays.asList("Java", "Spring", "后端")), 98));

        //模拟一个登录用户及其历史的偏好标签
        User currentUser = new Student("student_张","123456");

        System.out.println("=== 校园学习系统启动 ===");
    }
}
/*


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ResourceService resourceService = new ResourceService();

        // 初始化一些测试数据
        resourceService.addResource(new LearningResource(1, "Java面向对象基础笔记", "复习笔记", new HashSet<>(Arrays.asList("Java", "面向对象", "基础")), 85));
        resourceService.addResource(new LearningResource(2, "数据结构排序算法题解", "算法题解", new HashSet<>(Arrays.asList("DataStructure", "排序", "算法")), 92));
        resourceService.addResource(new LearningResource(3, "Spring Boot开源项目源码", "开源项目源码", new HashSet<>(Arrays.asList("Java", "Spring", "后端")), 98));

        // 模拟一个登录用户及其历史偏好标签
        User currentUser = new Student("student_zhang", "123456");
        Set<String> userTags = new HashSet<>(Arrays.asList("Java", "后端", "基础"));

        boolean running = true;
        while (running) {
            currentUser.showDashboard();
            System.out.print("请选择操作编号: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 消费换行符

            if (choice == 0) {
                running = false;
                System.out.println("感谢使用校园个性化学习资料智能推荐与检索系统！");
                break;
            }

            if (currentUser instanceof Student) {
                switch (choice) {
                    case 1:
                        System.out.print("请输入检索关键词 (如 Java / 排序): ");
                        String keyword = scanner.nextLine();
                        List<LearningResource> searchResults = resourceService.searchByKeyword(keyword);
                        System.out.println("检索结果: " + searchResults);
                        break;
                    case 2:
                        System.out.println("请选择推荐算法策略：1. Jaccard标签推荐  2. 热度推荐");
                        int strategyChoice = scanner.nextInt();
                        RecommendationStrategy strategy;
                        if (strategyChoice == 1) {
                            strategy = new JaccardRecommendationStrategy();
                        } else {
                            strategy = new HotnessRecommendationStrategy();
                        }
                        List<LearningResource> recs = strategy.recommend(userTags, resourceService.getAllResources(), 2);
                        System.out.println("为您推荐的 Top-N 资料: " + recs);
                        break;
                    case 3:
                        System.out.println("[AI助教] 您好！请输入您的学习疑问（例如：什么是Jaccard相似度？）");
                        String question = scanner.nextLine();
                        System.out.println("[AI助教回答] 规则引擎响应：关于“" + question + "”，建议复习面向对象与数据结构相关章节。");
                        break;
                    default:
                        System.out.println("无效的选择，请重新输入。");
                }
            }
        }
        scanner.close();
    }
}
 */