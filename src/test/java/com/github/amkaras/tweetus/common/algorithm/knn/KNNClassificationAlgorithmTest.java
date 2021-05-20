package com.github.amkaras.tweetus.common.algorithm.knn;

import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.algorithm.knn.model.DocumentWithTokenCompoundKey;
import com.github.amkaras.tweetus.common.algorithm.knn.model.DocumentsNotDeterminableException;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.amkaras.tweetus.common.algorithm.knn.model.Document.Builder.documentBuilder;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEGATIVE;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.NEUTRAL;
import static com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory.POSITIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class KNNClassificationAlgorithmTest {

    public static final Logger log = LoggerFactory.getLogger(KNNClassificationAlgorithmTest.class);

    private final StanfordLemmatizerClient lemmatizerClient = mock(StanfordLemmatizerClient.class);
    private final KNNClassificationAlgorithm knnAlgorithm = new KNNClassificationAlgorithm(lemmatizerClient);

    @Test
    public void shouldCorrectlyClassifyTweetForTwoPossibleCategories() {
        // given
        Document d1 = documentBuilder()
                .withContent("a great game")
                .withCategory(POSITIVE)
                .withHumanName("D1")
                .build();
        Document d2 = documentBuilder()
                .withContent("the election was over")
                .withCategory(NEGATIVE)
                .withHumanName("D2")
                .build();
        Document d3 = documentBuilder()
                .withContent("very clean match")
                .withCategory(POSITIVE)
                .withHumanName("D3")
                .build();
        Document d4 = documentBuilder()
                .withContent("a clean but forgettable game")
                .withCategory(POSITIVE)
                .withHumanName("D4")
                .build();
        Document d5 = documentBuilder()
                .withContent("it was a close election")
                .withCategory(NEGATIVE)
                .withHumanName("D5")
                .build();
        Set<Document> documents = Set.of(d1, d2, d3, d4, d5);

        Tweet tweet = new Tweet();
        tweet.setContent("a very close game");

        // when
        Map<Tweet, Optional<ClassificationCategory>> classificationFor1 =
                knnAlgorithm.classify(List.of(tweet), documents, false, 1);
        Optional<ClassificationCategory> maybeCategory1 = classificationFor1.get(tweet);
        // then
        assertTrue(maybeCategory1.isPresent());
        assertEquals(NEGATIVE, maybeCategory1.get());

        // when
        Map<Tweet, Optional<ClassificationCategory>> classificationFor3 =
                knnAlgorithm.classify(List.of(tweet), documents, false, 3);
        Optional<? extends ClassificationCategory> maybeCategory3 = classificationFor3.get(tweet);
        // then
        assertTrue(maybeCategory3.isPresent());
        assertEquals(POSITIVE, maybeCategory3.get());
    }

    @Test
    public void shouldCorrectlyClassifyTweetForThreePossibleCategories() {
        // given
        Document d1 = documentBuilder()
                .withContent("where the road goes")
                .withCategory(NEUTRAL)
                .withHumanName("D1")
                .build();
        Document d2 = documentBuilder()
                .withContent("where the day flows")
                .withCategory(NEGATIVE)
                .withHumanName("D2")
                .build();
        Document d3 = documentBuilder()
                .withContent("your heart chose")
                .withCategory(POSITIVE)
                .withHumanName("D3")
                .build();
        Document d4 = documentBuilder()
                .withContent("love might be in your heart")
                .withCategory(POSITIVE)
                .withHumanName("D4")
                .build();
        Document d5 = documentBuilder()
                .withContent("night keeps all your heart")
                .withCategory(NEGATIVE)
                .withHumanName("D5")
                .build();
        Set<Document> documents = Set.of(d1, d2, d3, d4, d5);

        Tweet tweet = new Tweet();
        tweet.setContent("when your love lies");

        // when
        Map<Tweet, Optional<ClassificationCategory>> classificationFor1 =
                knnAlgorithm.classify(List.of(tweet), documents, false, 1);
        Optional<ClassificationCategory> maybeCategory1 = classificationFor1.get(tweet);
        // then
        assertTrue(maybeCategory1.isPresent());
        assertEquals(POSITIVE, maybeCategory1.get());

        // when
        Map<Tweet, Optional<ClassificationCategory>> classificationFor3 =
                knnAlgorithm.classify(List.of(tweet), documents, false, 3);
        Optional<? extends ClassificationCategory> maybeCategory3 = classificationFor3.get(tweet);
        // then
        assertTrue(maybeCategory3.isPresent());
        assertEquals(POSITIVE, maybeCategory3.get());

        // when
        Map<Tweet, Optional<ClassificationCategory>> classificationFor5 =
                knnAlgorithm.classify(List.of(tweet), documents, false, 5);
        Optional<? extends ClassificationCategory> maybeCategory5 = classificationFor5.get(tweet);
        // then
        assertFalse(maybeCategory5.isPresent());
    }

    @Test
    public void shouldCorrectlyCalculateVectorsMatrix() {
        // given
        Document d1 = documentBuilder()
                .withContent("a great game")
                .withCategory(POSITIVE)
                .withHumanName("D1")
                .build();
        Document d2 = documentBuilder()
                .withContent("the election was over")
                .withCategory(NEGATIVE)
                .withHumanName("D2")
                .build();
        Document d3 = documentBuilder()
                .withContent("very clean match")
                .withCategory(POSITIVE)
                .withHumanName("D3")
                .build();
        Document d4 = documentBuilder()
                .withContent("a clean but forgettable game")
                .withCategory(POSITIVE)
                .withHumanName("D4")
                .build();
        Document d5 = documentBuilder()
                .withContent("it was a close election")
                .withCategory(NEGATIVE)
                .withHumanName("D5")
                .build();

        Document d6 = documentBuilder()
                .withContent("a very close game")
                .withHumanName("D6")
                .build();

        Set<Document> documents = Set.of(d1, d2, d3, d4, d5);

        Set<String> tokens = Set.of("a", "great", "game", "the", "election", "was", "over", "very",
                "clean", "match", "but", "forgettable", "it", "close");

        // when
        Map<DocumentWithTokenCompoundKey, Double> matrix =
                knnAlgorithm.vectorsMatrix(tokens, documents, Set.copyOf(documents));
        // then
        Map.ofEntries(
                Map.entry(new DocumentWithTokenCompoundKey(d1, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "great"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d2, "a"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "the"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "election"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "was"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "over"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d3, "a"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "very"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "clean"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "match"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d4, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "clean"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "but"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "forgettable"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d5, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "election"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "was"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "it"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "close"), 0.7)
        ).forEach((key, value) -> assertEquals(value, matrix.get(key), 0.1));

        // when
        Map<DocumentWithTokenCompoundKey, Double> vector =
                knnAlgorithm.vectorsMatrix(tokens, Set.of(d6), Set.copyOf(documents));
        // then
        Map.ofEntries(
                Map.entry(new DocumentWithTokenCompoundKey(d6, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "very"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "close"), 0.7)
        ).forEach((key, value) -> assertEquals(value, vector.get(key), 0.1));
    }

    @Test
    public void shouldCorrectlyCalculateTFIDF() {
        // given
        Document d1 = documentBuilder()
                .withContent("who can say where the road goes")
                .build();
        Document d2 = documentBuilder()
                .withContent("where the day flows only time")
                .build();
        Document d3 = documentBuilder()
                .withContent("and who can say if your love grows")
                .build();

        // when
        double tfidfForSay = knnAlgorithm.TFIDF("say", d1, Set.of(d1, d2, d3));
        // then
        assertEquals(0.1761, tfidfForSay, 0.001);

        // when
        double tfidfForRoad = knnAlgorithm.TFIDF("road", d1, Set.of(d1, d2, d3));
        // then
        assertEquals(0.4771, tfidfForRoad, 0.001);

        // when
        double tfidfForLove = knnAlgorithm.TFIDF("love", d1, Set.of(d1, d2, d3));
        // then
        assertEquals(0.0, tfidfForLove, 0.001);
    }

    @Test
    public void shouldCorrectlyFindClosestDocuments() throws DocumentsNotDeterminableException {
        // given
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();
        UUID fourthId = UUID.randomUUID();
        UUID fifthId = UUID.randomUUID();

        Map<UUID, Double> distances = Map.of(
                firstId, 0.05,
                secondId, 0.07,
                thirdId, 0.04,
                fourthId, 0.07,
                fifthId, 0.1
        );

        // when
        Set<UUID> closestIdsFor1 = knnAlgorithm.closestDocuments(distances, 1);
        // then
        assertEquals(Set.of(thirdId), closestIdsFor1);

        // when
        Set<UUID> closestIdsFor2 = knnAlgorithm.closestDocuments(distances, 2);
        // then
        assertEquals(Set.of(thirdId, firstId), closestIdsFor2);

        // when
        // then
        assertThrows(DocumentsNotDeterminableException.class, () -> knnAlgorithm.closestDocuments(distances, 3));

        // when
        Set<UUID> closestIdsFor4 = knnAlgorithm.closestDocuments(distances, 4);
        // then
        assertEquals(Set.of(thirdId, firstId, secondId, fourthId), closestIdsFor4);

        // when
        Set<UUID> closestIdsFor5 = knnAlgorithm.closestDocuments(distances, 5);
        // then
        assertEquals(Set.of(thirdId, firstId, secondId, fourthId, fifthId), closestIdsFor5);

        // when
        // then
        assertThrows(DocumentsNotDeterminableException.class, () -> knnAlgorithm.closestDocuments(distances, 6));
    }

    @Test
    public void shouldCorrectlyFindCategoryByNearestNeighbors() {
        // given
        Document d1 = documentBuilder()
                .withContent("a great game")
                .withCategory(POSITIVE)
                .withHumanName("D1")
                .build();
        Document d2 = documentBuilder()
                .withContent("the election was over")
                .withCategory(NEGATIVE)
                .withHumanName("D2")
                .build();
        Document d3 = documentBuilder()
                .withContent("very clean match")
                .withCategory(POSITIVE)
                .withHumanName("D3")
                .build();
        Document d4 = documentBuilder()
                .withContent("a clean but forgettable game")
                .withCategory(POSITIVE)
                .withHumanName("D4")
                .build();
        Document d5 = documentBuilder()
                .withContent("it was a close election")
                .withCategory(NEGATIVE)
                .withHumanName("D5")
                .build();

        Document d6 = documentBuilder()
                .withContent("a very close game")
                .withHumanName("D6")
                .build();

        Set<String> tokens = Set.of("a", "great", "game", "the", "election", "was", "over", "very",
                "clean", "match", "but", "forgettable", "it", "close");

        Map<DocumentWithTokenCompoundKey, Double> matrix = Map.ofEntries(
                Map.entry(new DocumentWithTokenCompoundKey(d1, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "great"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d1, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d2, "a"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "the"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "election"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "was"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "over"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d2, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d3, "a"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "very"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "clean"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "match"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d3, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d4, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "clean"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "but"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "forgettable"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d4, "close"), 0.0),

                Map.entry(new DocumentWithTokenCompoundKey(d5, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "game"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "election"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "was"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "very"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "it"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d5, "close"), 0.7)
        );
        Map<DocumentWithTokenCompoundKey, Double> vector = Map.ofEntries(
                Map.entry(new DocumentWithTokenCompoundKey(d6, "a"), 0.22),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "great"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "game"), 0.4),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "the"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "election"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "was"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "over"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "very"), 0.7),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "clean"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "match"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "but"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "forgettable"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "it"), 0.0),
                Map.entry(new DocumentWithTokenCompoundKey(d6, "close"), 0.7)
        );

        // when
        Stream.of(d1, d2, d3, d4, d5).forEach(document ->
                log.info("Document {}: {} [{}]", document.getId(), document.getContent(), document.getCategory()));
        log.info("Document {}: {}\n", d6.getId(), d6.getContent());
        Optional<? extends ClassificationCategory> categoryFor1 = knnAlgorithm.categoryByNearestNeighbors(tokens, matrix, vector, 1);
        // then
        assertTrue(categoryFor1.isPresent());
        assertEquals(NEGATIVE, categoryFor1.get());

        // when
        Stream.of(d1, d2, d3, d4, d5).forEach(document ->
                log.info("Document {}: {} [{}]", document.getId(), document.getContent(), document.getCategory()));
        log.info("Document {}: {}\n", d6.getId(), d6.getContent());
        Optional<? extends ClassificationCategory> categoryFor2 = knnAlgorithm.categoryByNearestNeighbors(tokens, matrix, vector, 2);
        // then
        assertFalse(categoryFor2.isPresent());

        // when
        Stream.of(d1, d2, d3, d4, d5).forEach(document ->
                log.info("Document {}: {} [{}]", document.getId(), document.getContent(), document.getCategory()));
        log.info("Document {}: {}\n", d6.getId(), d6.getContent());
        Optional<? extends ClassificationCategory> categoryFor3 = knnAlgorithm.categoryByNearestNeighbors(tokens, matrix, vector, 3);
        // then
        assertTrue(categoryFor3.isPresent());
        assertEquals(POSITIVE, categoryFor3.get());
    }

    @Test
    public void shouldCorrectlyCalculateDotProduct() {
        // given
        Map<DocumentWithTokenCompoundKey, Double> firstVector = Map.of(
                new DocumentWithTokenCompoundKey(mock(Document.class), "sign"), 0.1,
                new DocumentWithTokenCompoundKey(mock(Document.class), "of"), 0.0,
                new DocumentWithTokenCompoundKey(mock(Document.class), "the"), 0.5,
                new DocumentWithTokenCompoundKey(mock(Document.class), "times"), 0.0
        );
        Map<DocumentWithTokenCompoundKey, Double> secondVector = Map.of(
                new DocumentWithTokenCompoundKey(mock(Document.class), "sign"), 0.1,
                new DocumentWithTokenCompoundKey(mock(Document.class), "of"), 0.9,
                new DocumentWithTokenCompoundKey(mock(Document.class), "the"), 0.5,
                new DocumentWithTokenCompoundKey(mock(Document.class), "times"), 0.2
        );
        Set<String> tokens = Set.of("sign", "of", "the", "times");
        double expectedDotProduct = 0.26;
        // when
        double dotProduct = knnAlgorithm.dotProduct(firstVector, secondVector, tokens);

        // then
        assertEquals(expectedDotProduct, dotProduct, 0.0001);
    }

    @Test
    public void shouldCorrectlyCalculateMagnitude() {
        // given
        Set<Double> firstVectorValues = Set.of(1.0, 2.0, 3.0, 4.0);
        Set<Double> secondVectorValues = Set.of(2.0, 3.0, 4.0, 5.0);
        double expectedMagnitude = 40.2492;

        // when
        double magnitude = knnAlgorithm.magnitude(firstVectorValues, secondVectorValues);

        // then
        assertEquals(expectedMagnitude, magnitude, 0.0001);
    }

}