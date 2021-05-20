package com.github.amkaras.tweetus.common.util;

import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoggingUtils {

    public static final String WHITESPACE = " ";
    public static final String COMMA = ",";

    public static String formatDocumentsLog(Map<UUID, Document> universe, Set<UUID> documents) {
        List<String> formatted = new ArrayList<>();
        for (UUID id : documents) {
            if (universe.containsKey(id) && universe.get(id).getHumanName() != null) {
                formatted.add(universe.get(id).getHumanName() + WHITESPACE + id);
            } else {
                formatted.add(id.toString());
            }
        }
        return String.join(COMMA, formatted);
    }

    public static String formatDocumentsLog(Map<UUID, Document> universe, Map<UUID, Double> distancesForDocuments) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<UUID, Double> entry : distancesForDocuments.entrySet()) {
            if (universe.containsKey(entry.getKey()) && universe.get(entry.getKey()) != null) {
                result.put(universe.get(entry.getKey()).getHumanName() + WHITESPACE + entry.getKey().toString(), entry.getValue());
            } else {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result.toString();
    }
}
