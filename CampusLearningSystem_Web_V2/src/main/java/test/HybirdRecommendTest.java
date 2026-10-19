package test;


import com.campus.recommend.*;

import com.campus.model.LearningResource;
import com.campus.recommend.RecommendStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HybirdRecommendTest {

    public static void main(String[] args) {

        // 1. 创建三个测试资源
        List<LearningResource> resources = List.of(
                new LearningResource(101, "Java多态基础", "Java", 40),
                new LearningResource(102, "Java集合框架", "Java", 70),
                new LearningResource(103, "Python入门", "Python", 100)
        );

        // 2. 模拟三种推荐策略的评分结果
        // 使用 Lambda 表达式实现 RecommendStrategy 接口

        RecommendStrategy tagStrategy =
                (user, userTags, allResources) ->
                        Map.of(
                                101, 0.90,
                                102, 0.40,
                                103, 0.10
                        );

        RecommendStrategy cfStrategy =
                (user, userTags, allResources) ->
                        Map.of(
                                101, 0.80,
                                102, 0.90
                                // 故意不给103评分
                        );

        RecommendStrategy hotStrategy =
                (user, userTags, allResources) ->
                        Map.of(
                                101, 0.40,
                                102, 0.70,
                                103, 1.00
                        );

        // 3. 创建混合推荐策略
        RecommendStrategy hybrid = new HybirdRecommendStrategy(tagStrategy, cfStrategy, hotStrategy);

        // 4. 计算综合评分
        // 模拟策略不使用User，因此这里可以传null
        Map<Integer, Double> scores =
                hybrid.scoreAll(null, Set.of("Java"), resources);

        System.out.println("=== 混合推荐综合评分 ===");

        for (LearningResource resource : resources) {
            int id = resource.getId();

            System.out.printf(
                    "%s（%d）：%.2f%n",
                    resource.getTitle(),
                    id,
                    scores.get(id)
            );
        }

        // 5. 按综合评分从高到低排序
        List<Integer> sortedIds =
                new ArrayList<>(scores.keySet());

        sortedIds.sort(
                (a, b) -> Double.compare(
                        scores.get(b),
                        scores.get(a)
                )
        );

        System.out.println("\n=== Top-2 推荐资源ID ===");

        for (int i = 0; i < 2; i++) {
            System.out.println(sortedIds.get(i));
        }

        // 6. 自动检查结果  断言
        boolean passed =
                Math.abs(scores.get(101) - 0.76) < 0.000001
                        && Math.abs(scores.get(102) - 0.66) < 0.000001
                        && Math.abs(scores.get(103) - 0.24) < 0.000001
                        && sortedIds.get(0) == 101
                        && sortedIds.get(1) == 102;

        if (passed) {
            System.out.println(
                    "\n验收通过：权重融合、缺失分数和排序均正确！");
        } else {
            System.out.println("\n验收失败，请检查混合推荐算法！");
        }
    }
}

