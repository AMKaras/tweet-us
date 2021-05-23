package com.github.amkaras.tweetus.common.algorithm.knn;

import com.github.amkaras.tweetus.common.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.algorithm.knn.model.DocumentWithTokenCompoundKey;
import com.github.amkaras.tweetus.common.algorithm.knn.model.DocumentsNotDeterminableException;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.util.Tokenizer;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.amkaras.tweetus.common.algorithm.knn.model.Document.Builder.documentBuilder;
import static com.github.amkaras.tweetus.common.util.LoggingUtils.formatDocumentsLog;
import static com.github.amkaras.tweetus.common.util.Tokenizer.join;
import static com.github.amkaras.tweetus.common.util.Tokenizer.prepareTokens;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class KNNClassificationAlgorithm implements ClassificationAlgorithm {

    public static final Logger log = LoggerFactory.getLogger(KNNClassificationAlgorithm.class);

    private final StanfordLemmatizerClient lemmatizerClient;
    private Map<UUID, Document> documentsUniverse = new HashMap<>();
    private List<Tweet> tweets = new CopyOnWriteArrayList<>();

    public KNNClassificationAlgorithm(StanfordLemmatizerClient lemmatizerClient) {
        this.lemmatizerClient = lemmatizerClient;
    }

    @Override
    public Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary, boolean lemmatizationEnabled) {
        throw new UnsupportedOperationException("Classification with given parameters is not supported using KNN algorithm");
    }

    @Override
    public Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Set<Document> classifiedDocuments, boolean lemmatizationEnabled, int k) {

        this.documentsUniverse = classifiedDocuments.stream()
                .collect(toMap(Document::getId, identity()));

        tweets = tweets.stream()
                .peek(tweet -> tweet.setContent(join(prepareTokens(tweet))))
                .collect(toList());

        if (lemmatizationEnabled) {
            tweets = tweets.stream()
                    .peek(tweet -> {
                        log.info("Non lemmatized tweet {}: {}", tweet.getId(), tweet.getContent());
                        String lemmatized = join(lemmatizerClient.lemmatize(tweet.getContent()));
                        tweet.setContent(lemmatized);
                        log.info("Lemmatized tweet {}: {}", tweet.getId(), tweet.getContent());
                    })
                    .collect(toList());
        }

        Set<String> distinctTokens = classifiedDocuments.stream()
                .map(Document::getContent)
                .map(Tokenizer::prepareTokens)
                .flatMap(List::stream)
                .collect(toSet());

        Map<DocumentWithTokenCompoundKey, Double> vectorsMatrix =
                vectorsMatrix(distinctTokens, classifiedDocuments, Set.copyOf(documentsUniverse.values()));

        this.tweets = tweets;
        return tweets.stream().collect(toMap(identity(),
                tweet -> classifyTweet(tweet, distinctTokens, vectorsMatrix, classifiedDocuments, k)));
    }

    Map<DocumentWithTokenCompoundKey, Double> vectorsMatrix(Set<String> distinctTokens,
                                                            Set<Document> documentsToBeIncludedInMatrix,
                                                            Set<Document> documentsUniverse) {
        Map<DocumentWithTokenCompoundKey, Double> vectorsMatrix = new LinkedHashMap<>();
        for (Document document : documentsToBeIncludedInMatrix) {
            for (String token : distinctTokens) {
                vectorsMatrix.put(new DocumentWithTokenCompoundKey(document, token), TFIDF(token, document, documentsUniverse));
            }
        }
        log.info("Vectors matrix is: {}", vectorsMatrix);
        return vectorsMatrix;
    }

    double TFIDF(String distinctToken, Document document, Set<Document> documentsUniverse) {
        double tf = (double) Tokenizer.splitOnly(document.getContent()).stream()
                .filter(maybeToken -> maybeToken.equals(distinctToken))
                .count();
        double df = (double) documentsUniverse.stream()
                .map(Document::getContent)
                .map(Tokenizer::splitOnly)
                .flatMap(List::stream)
                .filter(content -> content.equals(distinctToken))
                .count();
        double n = documentsUniverse.size();
        double logarithm = Math.log10(n / df);
        double tfidf = tf * logarithm;
        log.debug("Document{} {}: \"{}\", Token: \"{}\", TF-IDF: {}, TF: {}, DF: {}, Documents count: {}, Logarithm: {}",
                document.getHumanName() == null ? "" : " " + document.getHumanName(), document.getId(),
                document.getContent(), distinctToken, tfidf, tf, df, n, logarithm);
        return tfidf;
    }

    private Optional<ClassificationCategory> classifyTweet(Tweet tweet, Set<String> distinctTokens,
                                                           Map<DocumentWithTokenCompoundKey, Double> vectorsMatrix,
                                                           Set<Document> classifiedDocuments, int k) {
        Document documentFromTweet = documentBuilder()
                .withContent(tweet.getContent())
                .build();
        Map<DocumentWithTokenCompoundKey, Double> vectorsMatrixForTestDocument =
                vectorsMatrix(distinctTokens, Set.of(documentFromTweet), classifiedDocuments);
        log.info("Vectors matrix for test document is: {}", vectorsMatrixForTestDocument);
        Optional<ClassificationCategory> category =
                categoryByNearestNeighbors(distinctTokens, vectorsMatrix, vectorsMatrixForTestDocument, k);
        log.info("Classified {} out of {} tweets", this.tweets.indexOf(tweet) + 1, this.tweets.size());
        return category;
    }

    Optional<ClassificationCategory> categoryByNearestNeighbors(
            Set<String> distinctTokens, Map<DocumentWithTokenCompoundKey, Double> vectorsMatrixForClassifiedDocuments,
            Map<DocumentWithTokenCompoundKey, Double> vectorForTestDocument, int k) {

        Set<UUID> documentIds = vectorsMatrixForClassifiedDocuments.keySet().stream()
                .map(key -> key.getDocument().getId())
                .collect(toSet());

        if (k > documentIds.size()) {
            log.error("K parameter cannot be greater than documents count which is {}", documentIds.size());
            return Optional.empty();
        }

        Map<UUID, Double> distancesFromDocuments = new HashMap<>();
        for (UUID id : documentIds) {
            Map<DocumentWithTokenCompoundKey, Double> vectorForClassifiedDocument =
                    vectorsMatrixForClassifiedDocuments.entrySet().stream()
                            .filter(entry -> entry.getKey().getDocument().getId().equals(id))
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            double dotProduct =
                    dotProduct(vectorForTestDocument, vectorForClassifiedDocument, distinctTokens);
            double magnitude =
                    magnitude(vectorForTestDocument.values(), vectorForClassifiedDocument.values());
            double cosineSimilarity = dotProduct / magnitude;
            double cosineDistance = 1.0d - cosineSimilarity;
            log.info("Document{} {}. Cosine distance: {}, cosine similarity: {}, dot product: {}, magnitude: {}",
                    !documentsUniverse.containsKey(id) || documentsUniverse.get(id).getHumanName() == null ?
                            "" : " " + documentsUniverse.get(id).getHumanName(),
                    id, cosineDistance, cosineSimilarity, dotProduct, magnitude);
            distancesFromDocuments.put(id, cosineDistance);
        }
        log.info("Distances are: {}", formatDocumentsLog(documentsUniverse, distancesFromDocuments));

        try {
            Set<UUID> closestDocuments = closestDocuments(distancesFromDocuments, k);
            log.info("Closest documents: {}", formatDocumentsLog(documentsUniverse, closestDocuments));
            Map<ClassificationCategory, Integer> closestCategoriesCount = closestDocuments.stream()
                    .map(id -> vectorsMatrixForClassifiedDocuments.keySet().stream()
                            .filter(key -> key.getDocument().getId().equals(id))
                            .findAny()
                            .orElseThrow(IllegalStateException::new)
                            .getDocument()
                            .getCategory())
                    .collect(toMap(identity(), c -> 1, Integer::sum));
            log.info("Closest categories: {}", closestCategoriesCount);
            if (closestCategoriesCount.size() < 1) {
                throw new IllegalStateException();
            } else if (closestCategoriesCount.size() == 1) {
                ClassificationCategory category = closestCategoriesCount.entrySet().stream()
                        .findFirst()
                        .get()
                        .getKey();
                return Optional.of(category);
            } else {
                OptionalInt maxCount = closestCategoriesCount.values().stream()
                        .mapToInt(Integer::intValue)
                        .max();
                List<ClassificationCategory> categoriesMatchingMaxCount = closestCategoriesCount.entrySet().stream()
                        .filter(entry -> maxCount.getAsInt() == entry.getValue())
                        .map(Map.Entry::getKey)
                        .collect(toList());
                return categoriesMatchingMaxCount.size() == 1 ?
                        Optional.of(categoriesMatchingMaxCount.get(0)) : Optional.empty();
            }
        } catch (DocumentsNotDeterminableException e) {
            log.error("Unable to determine closest documents for parameter K = {}. " +
                    "Exception message is: {}", k, e.getMessage());
            return Optional.empty();
        }
    }

    Set<UUID> closestDocuments(Map<UUID, Double> distancesFromDocuments, int k)
            throws DocumentsNotDeterminableException {
        if (k > distancesFromDocuments.size()) {
            throw new DocumentsNotDeterminableException("K parameter too large");
        } else if (k == distancesFromDocuments.size()) {
            return distancesFromDocuments.keySet();
        }
        List<Double> sortedDistances = distancesFromDocuments.values().stream()
                .sorted()
                .collect(toList());
        if (sortedDistances.get(k - 1).equals(sortedDistances.get(k))) {
            throw new DocumentsNotDeterminableException("Not unique result for K parameter");
        }
        List<Double> validDistances = sortedDistances.stream()
                .limit(k)
                .collect(toList());
        return distancesFromDocuments.entrySet().stream()
                .filter(entry -> validDistances.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    double dotProduct(Map<DocumentWithTokenCompoundKey, Double> vectorForClassifiedDocument,
                      Map<DocumentWithTokenCompoundKey, Double> vectorForTestDocument,
                      Set<String> distinctTokens) {
        double sum = 0.0;
        for (String token : distinctTokens) {
            double weightForClassifiedDocument = getWeightByToken(vectorForClassifiedDocument, token);
            double weightForTestDocument = getWeightByToken(vectorForTestDocument, token);
            sum += (weightForClassifiedDocument * weightForTestDocument);
        }
        return sum;
    }

    double magnitude(Collection<Double> valuesFromVectorForTestDocument,
                     Collection<Double> valuesForVectorForClassifiedDocument) {
        double documentDistancesSquared = valuesFromVectorForTestDocument.stream()
                .mapToDouble(value -> Math.pow(value, 2))
                .sum();
        double baseDistancesSquared = valuesForVectorForClassifiedDocument.stream()
                .mapToDouble(value -> Math.pow(value, 2))
                .sum();
        return Math.sqrt(documentDistancesSquared) * Math.sqrt(baseDistancesSquared);
    }

    private double getWeightByToken(Map<DocumentWithTokenCompoundKey, Double> vector, String token) {
        return vector.entrySet().stream()
                .filter(entry -> entry.getKey().getToken().equals(token))
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getValue();
    }
}
