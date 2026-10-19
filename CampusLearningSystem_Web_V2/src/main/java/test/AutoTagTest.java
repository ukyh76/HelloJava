package test;

import com.campus.model.LearningResource;
import com.campus.model.Role;
import com.campus.model.Teacher;
import com.campus.model.User;
import com.campus.service.ResourceService;

public class AutoTagTest {

    public static void main(String[] args) {

        // 1. 创建资源服务
        ResourceService resourceService = new ResourceService();

        // 2. 创建教师用户
        // 这里使用你项目中实际的 User 构造方法
        User teacher = new Teacher("teacher_li", "123456")/* 创建一个 Role.TEACHER 的用户 */;

        // 3. 创建学习资源（暂时不设置标签）
        LearningResource resource = new LearningResource(
                101,
                "Java多态与继承",
                "Java",
                0
        );

        // 4. 添加资源，自动调用AI生成标签
        boolean success = resourceService.addResource(
                teacher, resource
        );

        // 5. 检查AI生成的标签
        System.out.println("添加是否成功：" + success);
        System.out.println("资源标题：" + resource.getTitle());
        System.out.println("自动生成的标签：" + resource.getTags());

        // 6. 检查资源是否保存成功
        LearningResource saved =
                resourceService.getResourceById(101);

        System.out.println("保存后的资源：" + saved);

        // 7. 检查标签搜索
        System.out.println("搜索【多态】的结果：");

        for (LearningResource r :
                resourceService.searchByKeyword("多态")) {
            System.out.println(r);
        }
    }
}
