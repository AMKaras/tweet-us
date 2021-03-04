package com.github.amkaras.tweetus.bayes;

import com.github.amkaras.tweetus.bayes.category.BinaryClassificationCategory;
import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.bayes.category.ClassificationType;
import com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory;
import com.github.amkaras.tweetus.entity.opinionfinder.DictionaryEntry;
import com.github.amkaras.tweetus.entity.opinionfinder.OpinionFinderAnalysis;

import java.util.*;

import static com.github.amkaras.tweetus.bayes.category.ClassificationType.BINARY;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class DictionaryBuilder {

    public Map<ClassificationCategory, Map<String, Long>> build(
            List<OpinionFinderAnalysis> analyzedTweets, ClassificationType classificationType) {

        Map<ClassificationCategory, List<DictionaryEntry>> entriesPerCategory = analyzedTweets.stream()
                .map(OpinionFinderAnalysis::getDictionary)
                .flatMap(Set::stream)
                .collect(groupingBy(entry -> DifferentialClassificationCategory.getByName(entry.getPolarity())));

        if (BINARY == classificationType) {
            List<DictionaryEntry> negativeEntries = new ArrayList<>();
            negativeEntries.addAll(entriesPerCategory.get(STRONG_NEGATIVE));
            negativeEntries.addAll(entriesPerCategory.get(NEGATIVE));
            negativeEntries.addAll(entriesPerCategory.get(WEAK_NEGATIVE));
            List<DictionaryEntry> neutralEntries = entriesPerCategory.get(NEUTRAL);
            List<DictionaryEntry> positiveEntries = new ArrayList<>();
            positiveEntries.addAll(entriesPerCategory.get(STRONG_POSITIVE));
            positiveEntries.addAll(entriesPerCategory.get(POSITIVE));
            positiveEntries.addAll(entriesPerCategory.get(WEAK_POSITIVE));
            entriesPerCategory = Map.of(
                    BinaryClassificationCategory.NEGATIVE, negativeEntries,
                    BinaryClassificationCategory.NEUTRAL, neutralEntries,
                    BinaryClassificationCategory.POSITIVE, positiveEntries);
        }

        return entriesPerCategory.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> toDictionary(entry.getValue())));
    }

    private Map<String, Long> toDictionary(List<DictionaryEntry> entries) {

        final Map<String, Long> dictionary = new HashMap<>();

        for (DictionaryEntry entry : entries) {
            var token = entry.getToken().toLowerCase();
            long count = dictionary.containsKey(token) ? dictionary.get(token) : 0;
            dictionary.put(token, count + 1);
        }

        return dictionary;
    }
}
