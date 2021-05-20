package com.github.amkaras.tweetus.common.algorithm.bayes;

import com.github.amkaras.tweetus.common.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEUTRAL;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_POSITIVE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class NaiveBayesClassificationAlgorithmTest {

    private Tweet tweet;
    private Map<ClassificationCategory, Map<String, Long>> dictionary;
    private List<String> lemmatizedTokens;
    private Optional<ClassificationCategory> expectedCategory;

    private StanfordLemmatizerClient lemmatizerClient = mock(StanfordLemmatizerClient.class);
    private ClassificationAlgorithm algorithm;

    public NaiveBayesClassificationAlgorithmTest(Tweet tweet,
                                                 Map<ClassificationCategory, Map<String, Long>> dictionary,
                                                 List<String> lemmatizedTokens,
                                                 Optional<ClassificationCategory> expectedCategory) {
        this.tweet = tweet;
        this.dictionary = dictionary;
        this.lemmatizedTokens = lemmatizedTokens;
        this.expectedCategory = expectedCategory;
    }

    @Before
    public void initialize() {
        algorithm = new NaiveBayesClassificationAlgorithm(lemmatizerClient);
    }

    @Parameterized.Parameters
    public static Collection arguments() {
        return Arrays.asList(new Object[][]{
                {
                        tweetWithContent("Hey Jude, don't make it bad, take a sad song"),
                        Map.of(STRONG_NEGATIVE, Map.of("bad", 2L, "sad", 2L),
                                NEGATIVE, Map.of(),
                                WEAK_NEGATIVE, Map.of("dont", 1L),
                                NEUTRAL, Map.of("beatle", 1L),
                                WEAK_POSITIVE, Map.of("heart", 5L),
                                POSITIVE, Map.of(),
                                STRONG_POSITIVE, Map.of("good", 2L)),
                        List.of("Hey", "Jude", "dont", "make", "it", "bad", ",", "take", "a", "sad", "song"),
                        Optional.empty()
                },
                {
                        tweetWithContent("Hey Jude, don't make it bad, take a sad song and make it better"),
                        Map.of(STRONG_NEGATIVE, Map.of("bad", 2L, "sad", 2L),
                                NEGATIVE, Map.of("afraid", 2L),
                                WEAK_NEGATIVE, Map.of("dont", 1L),
                                NEUTRAL, Map.of("beatles", 1L),
                                WEAK_POSITIVE, Map.of("heart", 5L),
                                POSITIVE, Map.of("na na na na na na", 10L),
                                STRONG_POSITIVE, Map.of("good", 2L)),
                        List.of("Hey", "Jude", "dont", "make", "it", "bad", ",", "take", "a", "sad", "song", "and", "make", "it", "good"),
                        Optional.of(WEAK_NEGATIVE)
                }
        });
    }

    @Test
    public void shouldCorrectlyClassifyTweet() {
        when(lemmatizerClient.lemmatize(tweet.getContent())).thenReturn(lemmatizedTokens);

        assertEquals(expectedCategory, algorithm.classify(List.of(tweet), dictionary, true).get(tweet));
    }

    private static Tweet tweetWithContent(String content) {
        Tweet tweet = new Tweet();
        tweet.setContent(content);
        return tweet;
    }
}