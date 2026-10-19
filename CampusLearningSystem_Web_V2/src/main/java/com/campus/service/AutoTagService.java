package com.campus.service;

import com.campus.ai.AiAssistant;
import com.campus.ai.LlmAssistant;

import java.util.Arrays;
import java.util.List;

public class AutoTagService {
    private final AiAssistant ai;

    public AutoTagService(AiAssistant ai) {
        this.ai = ai;
    }
    public List<String> generateTags(String title, String description){
        String prompt = """
                请根据以下学习资源生成3到5个知识标签。
                只返回标签，使用英文逗号分隔。
                不要添加解释。
                资源标题:%s
                资源简介:%s
                """.formatted(title,description);
        String answer = ai.ask(prompt);
        String parts[] = answer.split(",");
        return Arrays.asList(parts);
    }
    public static void main(String[] args) {

        // 创建AI助手
        AiAssistant assistant = new LlmAssistant();

        // 创建智能标签服务
        AutoTagService tagService =
                new AutoTagService(assistant);

        // 模拟教师上传学习资源
        String title = "Java面向对象程序设计——多态";

        String description =
                "介绍Java继承、方法重写、向上转型和运行时多态。";

        // 自动生成标签
        List<String> tags =
                tagService.generateTags(title, description);

        // 显示结果
        System.out.println("资源标题：" + title);
        System.out.println("AI生成的标签：");

        for (String tag : tags) {
            System.out.println(tag);
        }
    }
}
