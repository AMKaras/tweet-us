package com.github.amkaras.tweetus.common.algorithm.bayes;

import com.github.amkaras.tweetus.common.model.BinaryClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory;
import com.github.amkaras.tweetus.external.opinionfinder.entity.DictionaryEntry;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.github.amkaras.tweetus.common.model.ClassificationType.BINARY;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEUTRAL;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_POSITIVE;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.atLeastThreeCharacters;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.containsWhitespace;

public class DictionaryBuilder {

    private static final Logger log = LoggerFactory.getLogger(DictionaryBuilder.class);

    private final StanfordLemmatizerClient lemmatizerClient;

    public DictionaryBuilder(StanfordLemmatizerClient lemmatizerClient) {
        this.lemmatizerClient = lemmatizerClient;
    }

    public Map<ClassificationCategory, Map<String, Long>> build(
            List<OpinionFinderAnalysis> analyzedTweets,
            ClassificationType classificationType,
            boolean lemmatizationEnabled) {

        Map<ClassificationCategory, List<DictionaryEntry>> entriesPerCategory = analyzedTweets.stream()
                .map(OpinionFinderAnalysis::getDictionary)
                .flatMap(Set::stream)
                .collect(groupingBy(entry -> DifferentialClassificationCategory.getByName(entry.getPolarity())));

        if (BINARY == classificationType) {
            List<DictionaryEntry> negativeEntries = new ArrayList<>();
            reduceNotNullDifferentialCategoriesConsumer(negativeEntries, STRONG_NEGATIVE, NEGATIVE, WEAK_NEGATIVE)
                    .accept(entriesPerCategory);
            List<DictionaryEntry> neutralEntries = new ArrayList<>();
            reduceNotNullDifferentialCategoriesConsumer(neutralEntries, NEUTRAL).accept(entriesPerCategory);
            List<DictionaryEntry> positiveEntries = new ArrayList<>();
            reduceNotNullDifferentialCategoriesConsumer(positiveEntries, STRONG_POSITIVE, POSITIVE, WEAK_POSITIVE)
                    .accept(entriesPerCategory);
            entriesPerCategory = Map.of(
                    BinaryClassificationCategory.NEGATIVE, negativeEntries,
                    BinaryClassificationCategory.NEUTRAL, neutralEntries,
                    BinaryClassificationCategory.POSITIVE, positiveEntries);
        }

        var dictionary = entriesPerCategory.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> toDictionary(entry.getValue(), lemmatizationEnabled)));
        if (BINARY == classificationType) {
            validateDictionary(dictionary, BinaryClassificationCategory.values());
        } else {
            validateDictionary(dictionary, DifferentialClassificationCategory.values());
        }
        log.debug("Created dictionary: {}", dictionary);
        return new TreeMap<>(dictionary);
    }

    private Map<String, Long> toDictionary(List<DictionaryEntry> entries, boolean lemmatizationEnabled) {

        final Map<String, Long> dictionary = new TreeMap<>();

        for (DictionaryEntry entry : entries) {
            var token = entry.getToken().toLowerCase();
            if (lemmatizationEnabled && !containsWhitespace(token)) {
                var lemmatizedToken = lemmatizerClient.lemmatize(token).get(0);
                if (!lemmatizedToken.equals(token)) {
                    log.info("Lemmatized token {} to {}", token, lemmatizedToken);
                    token = lemmatizedToken;
                }
            }
            if (atLeastThreeCharacters().test(token)) {
                long count = dictionary.containsKey(token) ? dictionary.get(token) : 0;
                dictionary.put(token, count + 1);
            }
        }

        return dictionary;
    }

    private Consumer<Map<ClassificationCategory, List<DictionaryEntry>>> reduceNotNullDifferentialCategoriesConsumer(
            List<DictionaryEntry> targetList, DifferentialClassificationCategory... categoriesToBeReduced) {
        return entries -> {
            for (ClassificationCategory category : categoriesToBeReduced) {
                var entriesByCategory = entries.get(category);
                if (entriesByCategory != null) {
                    targetList.addAll(entries.get(category));
                }
            }
        };
    }

    private void validateDictionary(Map<ClassificationCategory, Map<String, Long>> entriesPerCategory,
                                    ClassificationCategory[] expectedCategories) {
        if (!entriesPerCategory.keySet()
                .containsAll(Set.of(expectedCategories)) ||
                entriesPerCategory.values().stream().anyMatch(Map::isEmpty)) {
            throw new RuntimeException("Created dictionary either doesn't contain all expected categories " +
                    "or at least one of them is empty!");
        }
    }
}
