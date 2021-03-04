package com.github.amkaras.tweetus.bayes.algorithm;

import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.NEGATIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.NEUTRAL;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.POSITIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.STRONG_NEGATIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.STRONG_POSITIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.WEAK_NEGATIVE;
import static com.github.amkaras.tweetus.bayes.category.DifferentialClassificationCategory.WEAK_POSITIVE;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NaiveBayesClassificationAlgorithmTest {

    private Tweet tweet;
    private Map<ClassificationCategory, Map<String, Long>> dictionary;
    private ClassificationCategory expectedCategory;

    private ClassificationAlgorithm algorithm;

    public NaiveBayesClassificationAlgorithmTest(Tweet tweet,
                                                 Map<ClassificationCategory, Map<String, Long>> dictionary,
                                                 ClassificationCategory expectedCategory) {
        this.tweet = tweet;
        this.dictionary = dictionary;
        this.expectedCategory = expectedCategory;
    }

    @Before
    public void initialize() {
        algorithm = new NaiveBayesClassificationAlgorithm();
    }

    @Parameterized.Parameters
    public static Collection arguments() {
        return Arrays.asList(new Object[][]{
                {
                        tweetWithContent("Hey Jude, don't make it bad, take a sad song"),
                        Map.of(STRONG_NEGATIVE, Map.of("bad", 2L, "sad", 2L),
                                NEGATIVE, Map.of(),
                                WEAK_NEGATIVE, Map.of("dont", 1L),
                                NEUTRAL, Map.of("beatles", 1L),
                                WEAK_POSITIVE, Map.of("heart", 5L),
                                POSITIVE, Map.of(),
                                STRONG_POSITIVE, Map.of("better", 2L)),
                        STRONG_NEGATIVE
                }
        });
    }

    @Test
    public void shouldCorrectlyCalculateWeightedScoreAndAssignProperCategory() {
        assertEquals(expectedCategory, algorithm.classify(List.of(tweet), dictionary).get(tweet));
    }

    private static Tweet tweetWithContent(String content) {
        Tweet tweet = new Tweet();
        tweet.setContent(content);
        return tweet;
    }
}