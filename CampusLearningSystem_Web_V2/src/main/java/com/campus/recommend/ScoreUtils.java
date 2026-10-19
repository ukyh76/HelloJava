package com.campus.recommend;

import java.util.LinkedHashMap;
import java.util.Map;
//归一化
/*
final class ScoreUtils {
    private ScoreUtils() { }
    static Map<Integer, Double> normalize(Map<Integer, Double> raw) {
        double max = raw.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        Map<Integer, Double> result = new LinkedHashMap<>();
        raw.forEach((id, score) -> result.put(id, max <= 0 ? 0.0 : score / max));
        return result;
    }
}*/
final class ScoreUtils {
    private ScoreUtils() {
    }

    static Map<Integer, Double> normalize(Map<Integer, Double> rawScores) {
        Map<Integer, Double> normalizedScores = new LinkedHashMap<>();
        if (rawScores == null || rawScores.isEmpty()) {
            return normalizedScores;
        }

        double maxScore = rawScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        rawScores.forEach((id, score) -> normalizedScores.put(
                id, maxScore <= 0.0 ? 0.0 : score / maxScore));
        return normalizedScores;
    }
}
