package com.campus.controller;

import com.campus.dao.impl.InMemoryUserDao;

import com.campus.face.MockFaceRecognitionService;
import com.campus.model.LearningResource;
import com.campus.model.Student;
import com.campus.model.Teacher;
import com.campus.model.User;
import com.campus.recommend.HotnessRecommendStrategy;
import com.campus.recommend.RecommendStrategy;
import com.campus.recommend.TagSimilarityStrategy;
import com.campus.service.ResourceService;
import com.campus.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ConsoleApp {
    private UserService userService = new UserService(new InMemoryUserDao(),new MockFaceRecognitionService());
    private ResourceService resourceService = new ResourceService();
   // private AiAssistant aiAssistant = new AiAssistant();
    private Scanner scanner = new Scanner(System.in);

    // 初始化测试数据
    public void initDemoData() {
        Teacher teacher = new Teacher("teacher_li", "123456");
        userService.register(teacher);

        Student student = new Student("stu_zhang", "123456");
        student.setFaceImage( "stu_zhang-face.jpg");
        userService.register(student);
        userService.addUserTag("stu_zhang", "Java");
        userService.addUserTag("stu_zhang", "算法");

        LearningResource r1 = new LearningResource(1, "Java面向对象核心编程笔记", "笔记", 85);
        r1.addTag("Java");
        r1.addTag("面向对象");
        resourceService.addResource(teacher,r1);


        LearningResource r2 = new LearningResource(2, "数据结构与经典算法精讲", "课本", 95);
        r1.addTag("数据结构");
        r1.addTag("算法");
        resourceService.addResource(teacher,r2);
        System.out.println("【系统提示】初始化测试数据加载完成！");
    }

    public void start(){
        initDemoData();
        System.out.println("==================================================");
        System.out.println("    欢迎使用校园个性化学习推荐与检索系统 (Java实训版)     ");
        System.out.println("==================================================");
        while(true){
            showMainMenu();
            String choice = scanner.nextLine().trim();
            switch(choice){
                case "1":
                        handleLogin();break;
                case "2":
                        handleRegister();break;
                case "3":
                    System.out.println("感谢使用，系统已安全退出，再见!"); return ;
                default:
                    System.out.println("输入错误，选项无效，请重新输入!");
            }

        }

    }

    private void handleRegister() {
        System.out.println("\n------用户注册页面------");
        System.out.println("请输入用户名:");
        String username = scanner.nextLine().trim();
        System.out.println("请输入密码:");
        String password = scanner.nextLine().trim();
        System.out.println("请选择角色类型：\n" +
                "1. 学生 student\n" +
                "2. 教师 teacher");
        String roleChoice = scanner.nextLine().trim();
        User newUser;
        if("2".equals(roleChoice)){
            newUser = new Teacher(username,password);
        }
        else
            newUser = new Student(username,password);
        userService.register(newUser);


    }
    //用户登录功能
    private void handleLogin(){
        System.out.println("\n--- 用户登录页面 ---");
        System.out.println("\n1.密码登录");
        System.out.println("\n2.人脸识别登录");
        System.out.println("\n请输入登录方式选项：");
        String loginType=  scanner.nextLine();
        System.out.print("请输入用户名:");
        String username  = scanner.nextLine();
        User currentUser ;
        if("2".equals(loginType)){
            System.out.print("请输入人脸图片文件名:");
            String loginImage = scanner.nextLine().trim();
            currentUser = userService.loginByFace(username,loginImage);

        }
        else {
            System.out.print("请输入密码:");
            String password = scanner.nextLine().trim();
            currentUser  = userService.login(username,password);
        }
        if(currentUser !=null){
            if(currentUser instanceof  Student){
                studentDashboard( (Student) currentUser);

            }
            if(currentUser instanceof  Teacher)
                teacherDashboard((Teacher) currentUser);
        }
    }

    private void handleLogin2() {
        System.out.println("\n--- 用户登录页面 ---");
        System.out.print("请输入用户名: ");
        String username = scanner.nextLine().trim();
        System.out.print("请输入密码: ");
        String password = scanner.nextLine().trim();
        User currentUser = userService.login(username,password);
        if(currentUser != null){
            //根据用户的具体角色，显示对应的菜单选项
            if(currentUser instanceof Teacher){
                teacherDashboard((Teacher) currentUser);
            }
            else if(currentUser instanceof  Student){
                studentDashboard((Student) currentUser);
            }
        }
    }

    private void showMainMenu(){
        System.out.println("\n--- 请选择操作 ---");
        System.out.println("1. 用户登录");
        System.out.println("2. 用户注册");
        System.out.println("3. 退出系统");
        System.out.print("请输入选项 (1-3): ");
    }
    //Teacher教师后台面板
    private void teacherDashboard(Teacher teacher){
        while(true){
            System.out.println("\n========教师管理面板=========\n当前用户:"+teacher.getUsername()+"\n");
            System.out.println("1. 录入新的学习资料");
            System.out.println("2. 查看系统所有学习资料");
            System.out.println("3. 删除指定学习资料");
            System.out.println("4. 退出登录（返回主菜单）");
            System.out.print("请输入选项 (1-4): ");

            String choice = scanner.nextLine().trim();
            switch(choice){
                case "1":
                    System.out.println("请输入资料ID(整数):");
                    int id = scanner.nextInt();
                    System.out.println("请输入资料标题:");
                    String title = scanner.nextLine();
                    System.out.println("请输入资料类别:");
                    String category = scanner.nextLine();
                    System.out.println("请输入热度分（整数）:");
                    int heat =  scanner.nextInt();

                    LearningResource resource = new LearningResource(id,title,category,heat);
                    System.out.println("请输入特征标签(逗号隔开)，如Java，算法:");
                    for (String tag:scanner.nextLine().split(",")){
                        if(!tag.trim().isEmpty())
                            resource.addTag(tag);
                    }

                    resourceService.addResource(teacher,resource);
                    break;
                case "2":
                    List<LearningResource> all = resourceService.getAllResource();
                    System.out.println("--所有资料共"+all.size()+"条--");
                    for(LearningResource r:all){
                        System.out.println(r.toString());
                    }
                    break;
                case "3":
                    System.out.println("输入要删除的资料ID:");
                    int delID = scanner.nextInt();
                    resourceService.deleteResource(teacher,delID);
                    break;
                case "4":
                    System.out.println("已经安全退出教师账号!");return ;
                default:
                    System.out.println("无效选项!");
            }
        }
    }
    //Student学生后台面板
    private void studentDashboard(Student student){
        while(true) {
            System.out.println("\n========学生学习面板=========\n当前用户:" + student.getUsername() + "\n");
            System.out.println("1. 关键词秒级检索资料 (倒排索引)");
            System.out.println("2. 查看与管理我的兴趣标签");
            System.out.println("3. 获取智能个性化推荐 (策略模式切换)");
            System.out.println("4. 呼叫 AI 学习助教");
            System.out.println("5. 退出登录（返回主菜单）");
            System.out.print("请输入选项 (1-5): ");
            String choice = scanner.nextLine().trim();
            switch(choice){
                case "1":
                    System.out.println("请输入检索关键词:");
                    String keyword = scanner.nextLine().trim();
                    List<LearningResource> searchResult = resourceService.searchByKeyword(keyword);
                    for(LearningResource resource:searchResult){
                        System.out.println("-->【ID："+resource.getId()+"】,"+resource.getTitle()+", 标签:"+resource.getTags());
                    }
                    break;
                case "2":
                    printTagMenu(student);break;
                case "3":
                    handleRecommendation(student);break;
                case "4":
                    //像AI提问功能还没有完成，后期补上
                   break;
                case "5":
                    System.out.println("已经安全退出学生账号!");return ;
                default:
                    System.out.println("无效选项!");
            }

        }
    }
    private void printTagMenu(Student student){
        System.out.println("\n----我的兴趣标签管理----");
        userService.printUserTag(student.getUsername());
        System.out.println("是否添加新标签？(输入1添加，其他返回)");
        if("1".equals(scanner.nextLine().trim())){
            System.out.println("请输入新标签名:");
            userService.addUserTag(student.getUsername(),scanner.nextLine().trim());
        }
    }
    private void handleRecommendation(Student student){
        System.out.println("\n--- 智能推荐引擎 ---");
        System.out.println("1. 标签相似度推荐 (Jaccard + 最大堆)");
        System.out.println("2. 全局热度降序推荐 (Hotness 策略)");
        System.out.print("请选择策略类型 (1-2): ");
        String type = scanner.nextLine();
        System.out.print("请输入推荐条数 Top-N: ");
        int n = Integer.parseInt(scanner.nextLine().trim());

        RecommendStrategy strategy = "2".equals(type)? new HotnessRecommendStrategy():new TagSimilarityStrategy();
        Set<String> tags = student.getTags();
        List<LearningResource> allResources = resourceService.getAllResource();
        Map<Integer,Double> resources = strategy.scoreAll(null,tags,allResources);

//        System.out.println("========== Top-"+n+" 推荐结果==========");
//        for (int i = 0; i < resources.size(); i++) {
//            LearningResource r = resources.get(i);
//            LearningResource r = resources.get()
//            System.out.println((i+1)+". 《"+r.getTitle()+" 》 | 热度： " +r.getHeatScore()+" | 标签:"+r.getTags() );
//
//        }
    }
}
