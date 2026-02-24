import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class StudentManager {
    private static final String DATA_FILE = "students.dat";
    private Map<String, Student> students = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);

    public StudentManager() {
        loadData();
    }

    public static void main(String[] args) {
        StudentManager manager = new StudentManager();
        manager.start();
    }

    public void start() {
        while (true) {
            showMenu();
            int choice = getIntInput("请输入您的选择: ");
            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    deleteStudent();
                    break;
                case 6:
                    saveData();
                    System.out.println("感谢使用学生管理系统，再见！");
                    scanner.close();
                    return;
                default:
                    System.out.println("输入错误，请重新输入！");
            }
        }
    }

    private void showMenu() {
        System.out.println("******** 欢迎来到学生管理系统 ********");
        System.out.println("1. 添加学生");
        System.out.println("2. 查看所有学生");
        System.out.println("3. 查询学生");
        System.out.println("4. 修改学生");
        System.out.println("5. 删除学生");
        System.out.println("6. 退出");
    }

    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextInt();
            } catch (Exception e) {
                System.out.println("输入错误，请输入数字！");
                scanner.next();
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextDouble();
            } catch (Exception e) {
                System.out.println("输入错误，请输入数字！");
                scanner.next();
            }
        }
    }

    private void addStudent() {
        System.out.println("===== 添加学生 =====");
        System.out.print("请输入学号: ");
        String id = scanner.next();
        if (students.containsKey(id)) {
            System.out.println("该学号已占用！");
            return;
        }
        System.out.print("请输入姓名: ");
        String name = scanner.next();
        int age = getIntInput("请输入年龄: ");
        double score = getDoubleInput("请输入成绩: ");
        Student student = new Student(id, name, age, score);
        students.put(id, student);
        System.out.println("添加成功！");
    }

    private void viewAllStudents() {
        System.out.println("===== 查看所有学生 =====");
        if (students.isEmpty()) {
            System.out.println("暂无学生信息");
            return;
        }
        System.out.println("学号\t姓名\t年龄\t成绩");
        System.out.println("-------------------------");
        for (Student student : students.values()) {
            System.out.println(student);
        }
    }

    private void searchStudent() {
        System.out.println("===== 查询学生 =====");
        System.out.print("请输入学号: ");
        String id = scanner.next();
        Student student = students.get(id);
        if (student == null) {
            System.out.println("未找到该学生！");
            return;
        }
        System.out.println("学号\t姓名\t年龄\t成绩");
        System.out.println("-------------------------");
        System.out.println(student);
    }

    private void updateStudent() {
        System.out.println("===== 修改学生 =====");
        System.out.print("请输入学号: ");
        String id = scanner.next();
        Student student = students.get(id);
        if (student == null) {
            System.out.println("未找到该学生！");
            return;
        }
        System.out.print("请输入新姓名: ");
        String name = scanner.next();
        int age = getIntInput("请输入新年龄: ");
        double score = getDoubleInput("请输入新成绩: ");
        student.setName(name);
        student.setAge(age);
        student.setScore(score);
        System.out.println("修改成功！");
    }

    private void deleteStudent() {
        System.out.println("===== 删除学生 =====");
        System.out.print("请输入学号: ");
        String id = scanner.next();
        if (!students.containsKey(id)) {
            System.out.println("未找到该学生！");
            return;
        }
        students.remove(id);
        System.out.println("删除成功！");
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Student> loadedStudents = (Map<String, Student>) ois.readObject();
            students.putAll(loadedStudents);
            System.out.println("数据加载成功！");
        } catch (Exception e) {
            System.out.println("数据加载失败: " + e.getMessage());
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(students);
            System.out.println("数据保存成功！");
        } catch (Exception e) {
            System.out.println("数据保存失败: " + e.getMessage());
        }
    }
}