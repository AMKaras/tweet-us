package com.github.amkaras.tweetus.common.algorithm.bayes;

import com.github.amkaras.tweetus.common.model.BinaryClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.external.opinionfinder.entity.DictionaryEntry;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.amkaras.tweetus.common.model.ClassificationType.BINARY;
import static com.github.amkaras.tweetus.common.model.ClassificationType.DIFFERENTIAL;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEUTRAL;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.STRONG_POSITIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.WEAK_POSITIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DictionaryBuilderTest {

    private List<OpinionFinderAnalysis> analyses;
    private ClassificationType classificationType;
    private Map<String, String> lemmatizations;
    private Map<ClassificationCategory, Map<String, Long>> expectedDictionary;

    private StanfordLemmatizerClient lemmatizerClient = mock(StanfordLemmatizerClient.class);

    private DictionaryBuilder dictionaryBuilder;

    @Before
    public void initialize() {
        this.dictionaryBuilder = new DictionaryBuilder(lemmatizerClient);
    }

    public DictionaryBuilderTest(List<OpinionFinderAnalysis> analyses,
                                 ClassificationType classificationType,
                                 Map<String, String> lemmatizations,
                                 Map<ClassificationCategory, Map<String, Long>> expectedDictionary) {
        this.analyses = analyses;
        this.classificationType = classificationType;
        this.lemmatizations = lemmatizations;
        this.expectedDictionary = expectedDictionary;
    }

    @Parameterized.Parameters
    public static Collection arguments() {
        return Arrays.asList(new Object[][]{
                {
                        List.of(
                                analysisOfEntries(
                                        dictionaryEntry("cold", "strongneg"),
                                        dictionaryEntry("cold", "strongneg"),
                                        dictionaryEntry("scars", "negative"),
                                        dictionaryEntry("ice", "weakneg"),
                                        dictionaryEntry("soul", "neutral"),
                                        dictionaryEntry("time", "weakpos"),
                                        dictionaryEntry("heart", "positive"),
                                        dictionaryEntry("love", "strongpos")
                                ),
                                analysisOfEntries(
                                        dictionaryEntry("soul", "neutral"),
                                        dictionaryEntry("soul", "neutral")
                                )
                        ),
                        DIFFERENTIAL,
                        Map.of(
                                "scars", "scar"
                        ),
                        Map.of(
                                STRONG_NEGATIVE, Map.of("cold", 2L),
                                NEGATIVE, Map.of("scar", 1L),
                                WEAK_NEGATIVE, Map.of("ice", 1L),
                                NEUTRAL, Map.of("soul", 3L),
                                WEAK_POSITIVE, Map.of("time", 1L),
                                POSITIVE, Map.of("heart", 1L),
                                STRONG_POSITIVE, Map.of("love", 1L)
                        )
                },
                {
                        List.of(
                                analysisOfEntries(
                                        dictionaryEntry("cold", "strongneg"),
                                        dictionaryEntry("cold", "strongneg"),
                                        dictionaryEntry("ice", "weakneg"),
                                        dictionaryEntry("soul", "neutral"),
                                        dictionaryEntry("soul", "neutral"),
                                        dictionaryEntry("soul", "neutral"),
                                        dictionaryEntry("heart", "positive")
                                )
                        ),
                        BINARY,
                        Map.of(),
                        Map.of(
                                BinaryClassificationCategory.NEGATIVE, Map.of(
                                        "cold", 2L,
                                        "ice", 1L
                                ),
                                BinaryClassificationCategory.NEUTRAL, Map.of("soul", 3L),
                                BinaryClassificationCategory.POSITIVE, Map.of("heart", 1L)
                        )
                },
                {
                        List.of(
                                analysisOfEntries(
                                        dictionaryEntry("I'm so excited", "strongpos"),
                                        dictionaryEntry("I'm so excited", "strongpos"),
                                        dictionaryEntry("like", "positive"),
                                        dictionaryEntry("think", "neutral"),
                                        dictionaryEntry("think", "neutral"),
                                        dictionaryEntry("hide", "weakneg")
                                )
                        ),
                        BINARY,
                        Map.of(),
                        Map.of(
                                BinaryClassificationCategory.NEGATIVE, Map.of("hide", 1L),
                                BinaryClassificationCategory.NEUTRAL, Map.of("think", 2L),
                                BinaryClassificationCategory.POSITIVE, Map.of(
                                        "i'm so excited", 2L,
                                        "like", 1L
                                )
                        )
                }
        });
    }

    @Test
    public void shouldCorrectlyBuildDictionary() {
        when(lemmatizerClient.lemmatize(anyString())).thenAnswer(i -> List.of(i.getArgument(0).toString()));
        lemmatizations.forEach((k, v) -> when(lemmatizerClient.lemmatize(k)).thenReturn(List.of(v)));

        assertEquals(expectedDictionary, dictionaryBuilder.build(analyses, classificationType, true));
    }

    @Test
    public void shouldNotAllowToCreateDictionaryWhenNotEnoughData() {
        List<OpinionFinderAnalysis> analysisNotEnoughForDictionary = List.of(analysisOfEntries(
                dictionaryEntry("love", "positive"), dictionaryEntry("alive", "neutral")
        ));
        assertThrows(RuntimeException.class, () -> dictionaryBuilder.build(analysisNotEnoughForDictionary, BINARY, true));
    }

    private static DictionaryEntry dictionaryEntry(String token, String polarity) {
        DictionaryEntry dictionaryEntry = new DictionaryEntry();
        dictionaryEntry.setToken(token);
        dictionaryEntry.setPolarity(polarity);
        return dictionaryEntry;
    }

    private static OpinionFinderAnalysis analysisOfEntries(DictionaryEntry... entries) {
        OpinionFinderAnalysis analysis = new OpinionFinderAnalysis();
        analysis.setDictionary(Set.of(entries));
        return analysis;
    }
}