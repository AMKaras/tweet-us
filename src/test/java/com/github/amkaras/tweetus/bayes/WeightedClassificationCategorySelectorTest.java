package com.github.amkaras.tweetus.bayes;

import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.opinionfinder.PolarityClassifier;
import com.github.amkaras.tweetus.entity.opinionfinder.SubjectiveClue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.POSITIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.WEAK_NEGATIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.WEAK_POSITIVE;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class WeightedClassificationCategorySelectorTest {

    private static final double DELTA = 0.01d;

    private Set<SubjectiveClue> clues;
    private Set<PolarityClassifier> polarities;
    private double expectedWeightedScore;
    private ClassificationCategory expectedClassificationCategory;

    private WeightedClassificationCategorySelector selector;

    public WeightedClassificationCategorySelectorTest(Set<SubjectiveClue> clues,
                                                      Set<PolarityClassifier> polarities,
                                                      double expectedWeightedScore,
                                                      ClassificationCategory expectedClassificationCategory) {
        this.clues = clues;
        this.polarities = polarities;
        this.expectedWeightedScore = expectedWeightedScore;
        this.expectedClassificationCategory = expectedClassificationCategory;
    }

    @Before
    public void initialize() {
        selector = new WeightedClassificationCategorySelector();
    }

    @Parameterized.Parameters
    public static Collection arguments() {
        return Arrays.asList(new Object[][]{
                {
                        subjectiveCluesFromEntries(Map.of("strongpos", 2, "weakpos", 1)),
                        polarityClassifiersFromEntries(Map.of("positive", 1)),
                        2.75d,
                        POSITIVE
                },
                {
                        subjectiveCluesFromEntries(Map.of("strongpos", 1, "neutral", 1)),
                        polarityClassifiersFromEntries(Map.of("neutral", 2)),
                        1.0d,
                        WEAK_POSITIVE
                },
                {
                        subjectiveCluesFromEntries(Map.of("weakneg", 2, "weakpos", 1, "strongpos", 1)),
                        polarityClassifiersFromEntries(Map.of()),
                        0.75d,
                        WEAK_POSITIVE
                },
                {
                        subjectiveCluesFromEntries(Map.of("weakpos", 2, "strongneg", 1)),
                        polarityClassifiersFromEntries(Map.of("negative", 1)),
                        -1.0d,
                        WEAK_NEGATIVE
                }
        });
    }

    @Test
    public void shouldCorrectlyCalculateWeightedScoreAndAssignProperCategory() {
        assertEquals(expectedWeightedScore, selector.weightedScore(clues, polarities), DELTA);
        assertEquals(expectedClassificationCategory, selector.select(clues, polarities));
    }

    private static Set<SubjectiveClue> subjectiveCluesFromEntries(Map<String, Integer> cluesWithCountMap) {
        return cluesWithCountMap.entrySet().stream()
                .map(entry -> {
                    var clue = new SubjectiveClue();
                    clue.setPolarity(entry.getKey());
                    clue.setCount(entry.getValue());
                    return clue;
                })
                .collect(toSet());
    }

    private static Set<PolarityClassifier> polarityClassifiersFromEntries(Map<String, Integer> polaritiesWithCountMap) {
        return polaritiesWithCountMap.entrySet().stream()
                .map(entry -> {
                    var polarity = new PolarityClassifier();
                    polarity.setScore(entry.getKey());
                    polarity.setCount(entry.getValue());
                    return polarity;
                })
                .collect(toSet());
    }
}