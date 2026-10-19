package com.campus.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LlmAssistant implements  AiAssistant{
    private final HttpClient client;
    private final String apiKey;
    private static final Pattern CONTENT_PATTERN = Pattern.compile(
            "\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");

    public LlmAssistant() {
        this.client =  HttpClient.newHttpClient();
        this.apiKey = System.getenv("MOONSHOT_API_KEY");

    }
    public static void main(String[] args) {

        AiAssistant assistant =
                new LlmAssistant();

        String answer =
                assistant.ask(
                        "请解释Java中接口和抽象类的区别"
                );

        System.out.println(answer);
    }

    @Override
    public String ask(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "未配置环境变量 MOONSHOT_API_KEY"
            );
        }
        try {
            String systemPrompt = "你是一名面向大学生的计算机课程学习助教。"
                    + "请使用适合初学者理解的语言，结合例子并优先提供Java示例。";
            String json = "{\"model\":\"moonshot-v1-8k\",\"messages\":["
                    + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                    + "{\"role\":\"user\",\"content\":\"" + escapeJson(question) + "\"}]}";
           //构造HTTP请求
            HttpRequest request = HttpRequest.newBuilder()

                    .uri(URI.create(
                            "https://api.moonshot.cn/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            //发送请求
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //检查HTTP状态
            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new RuntimeException("Kimi API调用失败，HTTP状态码："
                                + response.statusCode());
            }
            Matcher matcher = CONTENT_PATTERN.matcher(response.body());
            String answer = null;
            while (matcher.find()) {
                answer = unescapeJson(matcher.group(1));
            }
            if (answer == null) {
                throw new RuntimeException("AI 返回内容格式无法识别");
            }
            return answer;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("调用AI服务时线程被中断", e);

        } catch (Exception e) {
            throw new RuntimeException("调用AI学习助教失败", e);
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String unescapeJson(String value) {
        return value.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
/*

    @Override
    public String ask(String question) {


            // 6. 解析返回JSON
            JsonNode root =
                    mapper.readTree(response.body());

            // 7. 提取AI回答
            String answer = root
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            // 8. 返回答案
            return answer;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "调用AI服务时线程被中断", e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "调用AI学习助教失败", e
            );
        }
    }
}
 */
