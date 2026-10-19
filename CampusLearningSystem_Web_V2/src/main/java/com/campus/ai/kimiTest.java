package com.campus.ai;

/**
 * AI 助教的手动连通性测试。
 * 运行前需要设置 MOONSHOT_API_KEY。
 */
public class kimiTest {
    public static void main(String[] args) {
        AiAssistant assistant = new LlmAssistant();
        System.out.println(assistant.ask("请用一句话解释 Java 接口。"));
    }
}
