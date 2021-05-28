package com.github.amkaras.tweetus.common.logic;

import com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory;
import com.github.amkaras.tweetus.external.opinionfinder.entity.PolarityClassifier;
import com.github.amkaras.tweetus.external.opinionfinder.entity.SubjectiveClue;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class WeightedClassificationCategorySelector {

    private static final Logger log = LoggerFactory.getLogger(WeightedClassificationCategorySelector.class);
    private final Map<DifferentialClassificationCategory, Double> differentialCategoriesWithPolarities =
            Arrays.stream(DifferentialClassificationCategory.values())
                    .collect(toMap(identity(), __ -> Double.POSITIVE_INFINITY));

    public Optional<DifferentialClassificationCategory> select(Set<SubjectiveClue> clues, Set<PolarityClassifier> polarity) {
        return fromWeightedScore(weightedScore(clues, polarity));
    }

    private Optional<DifferentialClassificationCategory> fromWeightedScore(double weightedScore) {
        log.debug("Weighted score is {}", weightedScore);
        List<Map.Entry<DifferentialClassificationCategory, Double>> sorted =
                differentialCategoriesWithPolarities.entrySet().stream()
                        .peek(entry -> {
                            var distance = distance(entry.getKey().getPolarityScore(), weightedScore);
                            log.debug("Distance for category {} with polarity score {} and weighted score {} is {}",
                                    entry.getKey(), entry.getKey().getPolarityScore(), weightedScore, distance);
                            entry.setValue(distance);
                        })
                        .sorted(comparing(Map.Entry::getValue))
                        .collect(toList());
        if (sorted.get(0).getValue().equals(sorted.get(1).getValue())) {
            log.debug("Cannot choose category, distances are equal for categories {} and {}",
                    sorted.get(0).getKey(), sorted.get(1).getKey());
            return Optional.empty();
        }
        Map.Entry<DifferentialClassificationCategory, Double> winner = sorted.get(0);
        log.debug("Closest category with distance {} is {}", winner.getValue(), winner.getKey());
        return Optional.of(winner.getKey());
    }

    @VisibleForTesting
    double weightedScore(Set<SubjectiveClue> clues, Set<PolarityClassifier> polarities) {

        var cluesScore = clues.stream()
                .map(clue -> DifferentialClassificationCategory.getByName(clue.getPolarity()).getPolarityScore()
                        * clue.getCount())
                .mapToDouble(Double::doubleValue)
                .sum();
        var polaritiesScore = polarities.stream()
                .map(polarity -> DifferentialClassificationCategory.getByName(polarity.getScore()).getPolarityScore()
                        * polarity.getCount())
                .mapToDouble(Double::doubleValue)
                .sum();

        var cluesCount = clues.stream()
                .mapToInt(SubjectiveClue::getCount)
                .sum();
        var polaritiesCount = polarities.stream()
                .mapToInt(PolarityClassifier::getCount)
                .sum();

        return (cluesScore + polaritiesScore) / (cluesCount + polaritiesCount);
    }

    private double distance(double polarityScore, double weightedScore) {
        return Math.abs(polarityScore - weightedScore);
    }
}

