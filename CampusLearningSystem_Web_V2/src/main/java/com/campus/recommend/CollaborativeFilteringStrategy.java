package com.campus.recommend;

import com.campus.Repository.BehaviorRepository;
import com.campus.model.LearningResource;
import com.campus.model.User;
import com.campus.model.UserBehavior;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//协同过滤策略
public class CollaborativeFilteringStrategy implements RecommendStrategy {
    private final BehaviorRepository behaviors;

    public CollaborativeFilteringStrategy(BehaviorRepository behaviors) {
        this.behaviors = behaviors;
    }

    @Override
    public Map<Integer, Double> scoreAll(
            User user,
            Set<String> userTags,
            List<LearningResource> resources
    ) {
        Map<Integer, Double> predictions = new LinkedHashMap<>();
        if (user == null || resources == null || resources.isEmpty()) {
            return predictions;
        }

        Map<Integer, Map<Integer, Double>> matrix = buildRatingMatrix();
        Map<Integer, Double> current = matrix.getOrDefault(user.getUserID(), Map.of());
        Map<Integer, Double> weightedScores = new HashMap<>();
        Map<Integer, Double> similarityTotals = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, Double>> entry : matrix.entrySet()) {
            if (entry.getKey().equals(user.getUserID())) {
                continue;
            }

            double similarity = cosineSimilarity(current, entry.getValue());
            if (similarity <= 0) {
                continue;
            }

            for (Map.Entry<Integer, Double> rating : entry.getValue().entrySet()) {
                if (current.containsKey(rating.getKey())) {
                    continue;
                }
                weightedScores.merge(
                        rating.getKey(), similarity * rating.getValue(), Double::sum);
                similarityTotals.merge(rating.getKey(), similarity, Double::sum);
            }
        }

        for (LearningResource resource : resources) {
            double total = similarityTotals.getOrDefault(resource.getId(), 0.0);
            double score = total == 0.0
                    ? 0.0
                    : weightedScores.getOrDefault(resource.getId(), 0.0) / total;
            predictions.put(resource.getId(), score);
        }
        return ScoreUtils.normalize(predictions);
    }

    public Map<Integer, Map<Integer, Double>> buildRatingMatrix() {
        Map<Integer, Map<Integer, Double>> matrix = new HashMap<>();
        for (UserBehavior behavior : behaviors.findAll()) {
            matrix.computeIfAbsent(behavior.userID(), ignored -> new HashMap<>())
                    .merge(behavior.resourceID(), behavior.type().getWeight(), Double::sum);
        }
        return matrix;
    }

    public double cosineSimilarity(
            Map<Integer, Double> first,
            Map<Integer, Double> second
    ) {
        if (first == null || second == null || first.isEmpty() || second.isEmpty()) {
            return 0.0;
        }

        double firstNorm = 0.0;
        double secondNorm = 0.0;
        double dotProduct = 0.0;

        for (double value : first.values()) {
            firstNorm += value * value;
        }
        for (double value : second.values()) {
            secondNorm += value * value;
        }
        for (Map.Entry<Integer, Double> item : first.entrySet()) {
            dotProduct += item.getValue() * second.getOrDefault(item.getKey(), 0.0);
        }

        if (firstNorm == 0.0 || secondNorm == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }
}
