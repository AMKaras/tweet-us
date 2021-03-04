package com.github.amkaras.tweetus.bayes;

import com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory;
import com.github.amkaras.tweetus.entity.opinionfinder.PolarityClassifier;
import com.github.amkaras.tweetus.entity.opinionfinder.SubjectiveClue;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class WeightedClassificationCategorySelector {

    private static final Logger log = LoggerFactory.getLogger(WeightedClassificationCategorySelector.class);

    public DifferentialClassificationCategory select(Set<SubjectiveClue> clues, Set<PolarityClassifier> polarity) {

        var possibleCategoriesWithPolarities = Arrays.stream(DifferentialClassificationCategory.values())
                .collect(toMap(identity(), DifferentialClassificationCategory::getPolarityScore));

        return fromWeightedScore(weightedScore(clues, polarity), possibleCategoriesWithPolarities);
    }

    private DifferentialClassificationCategory fromWeightedScore(
            double weightedScore, Map<DifferentialClassificationCategory, Double> possibleCategoriesWithPolarities) {

        return possibleCategoriesWithPolarities.entrySet().stream()
                .peek(entry -> {
                    var distance = distance(entry.getValue(), weightedScore);
                    log.debug("Distance for category {} with polarity score {} and weighted score {} is {}",
                            entry.getKey(), entry.getKey().getPolarityScore(), weightedScore, distance);
                    entry.setValue(distance);
                })
                .min(comparing(Map.Entry::getValue))
                .get()
                .getKey();
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

