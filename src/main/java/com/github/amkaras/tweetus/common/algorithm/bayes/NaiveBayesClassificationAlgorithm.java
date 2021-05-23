package com.github.amkaras.tweetus.common.algorithm.bayes;

import com.github.amkaras.tweetus.common.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static com.github.amkaras.tweetus.common.util.FiltersFactory.atLeastThreeTokens;
import static com.github.amkaras.tweetus.common.util.Tokenizer.prepareTokens;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class NaiveBayesClassificationAlgorithm implements ClassificationAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassificationAlgorithm.class);

    private final StanfordLemmatizerClient lemmatizerClient;
    private List<Tweet> tweets = new CopyOnWriteArrayList<>();

    public NaiveBayesClassificationAlgorithm(StanfordLemmatizerClient lemmatizerClient) {
        this.lemmatizerClient = lemmatizerClient;
    }

    @Override
    public Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Set<Document> classifiedDocuments, boolean lemmatizationEnabled, int k) {
        throw new UnsupportedOperationException("Classification with given parameters is not supported using Naive Bayes algorithm");
    }

    @Override
    public Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary, boolean lemmatizationEnabled) {
        this.tweets = tweets;
        return tweets.stream()
                .map(tweet -> entry(tweet, mapToTokens(lemmatizationEnabled).apply(tweet)))
                .filter(atLeastThreeTokens())
                .collect(toMap(Entry::getKey,
                        tweetWithTokens -> chooseMostProbableCategory(tweetWithTokens, dictionary)));
    }

    private Function<Tweet, List<String>> mapToTokens(boolean lemmatizationEnabled) {
        return tweet -> lemmatizationEnabled ?
                prepareTokens(lemmatizerClient.lemmatize(tweet.getContent())) : prepareTokens(tweet);
    }

    private Optional<ClassificationCategory> chooseMostProbableCategory(
            Entry<Tweet, List<String>> tweetWithTokens,
            Map<ClassificationCategory, Map<String, Long>> dictionary) {

        log.info("Tweet {}: {}", tweetWithTokens.getKey().getId(), tweetWithTokens.getKey().getContent());
        final var tokens = tweetWithTokens.getValue();
        log.info("Tokens are {}", tokens);

        var laplaceSmoothingParameter = calculateLaplaceSmoothingParameter(dictionary);

        var categoriesByProbability = dictionary.entrySet().stream()
                .map(toCategoryWithProbabilityDecorator(tokens, laplaceSmoothingParameter))
                .sorted(comparing(CategoryWithProbabilityDecorator::getProbability, reverseOrder()))
                .collect(toList());

        var mostProbableCategory = selectCategory(categoriesByProbability);
        log.info("Tweet classified as {}", mostProbableCategory);
        log.info("Classified {} out of {} tweets", this.tweets.indexOf(tweetWithTokens.getKey()) + 1, this.tweets.size());
        return mostProbableCategory;
    }

    private static double perCategoryProbability(
            List<String> tokens, Map<String, Long> wordsWithOccurrences, int laplaceSmoothingParameter) {

        var divider = wordsWithOccurrences.values().stream()
                .mapToInt(Long::intValue)
                .sum()
                + laplaceSmoothingParameter;

        return tokens.stream()
                .map(token -> (double) (wordsWithOccurrences.getOrDefault(token, 0L) + 1L) / (double) divider)
                .mapToDouble(Double::doubleValue)
                .reduce(1.0, (a, b) -> a * b);
    }

    private int calculateLaplaceSmoothingParameter(Map<ClassificationCategory, Map<String, Long>> dictionary) {
        return dictionary.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    private Optional<ClassificationCategory> selectCategory(List<CategoryWithProbabilityDecorator> categoriesByProbability) {
        return categoriesByProbability.get(0).getProbability() == categoriesByProbability.get(1).getProbability() ?
                Optional.empty() : Optional.of(categoriesByProbability.get(0).getCategory());
    }

    private Function<Entry<ClassificationCategory, Map<String, Long>>, CategoryWithProbabilityDecorator> toCategoryWithProbabilityDecorator(
            List<String> tokens, int laplaceSmoothingParameter) {
        return entry -> CategoryWithProbabilityDecorator.of(entry.getKey(), tokens, entry.getValue(), laplaceSmoothingParameter);
    }

    private static final class CategoryWithProbabilityDecorator {

        private ClassificationCategory category;
        private double probability;

        public static CategoryWithProbabilityDecorator of(ClassificationCategory classificationCategory,
                                                          List<String> tokens,
                                                          Map<String, Long> wordsWithOccurrences,
                                                          int laplaceSmoothingParameter) {
            var decorator = new CategoryWithProbabilityDecorator(
                    classificationCategory, perCategoryProbability(tokens, wordsWithOccurrences, laplaceSmoothingParameter));
            log.info("Probability for category {} is {}", decorator.getCategory(), decorator.getProbability());
            return decorator;
        }

        private CategoryWithProbabilityDecorator(ClassificationCategory category, double probability) {
            this.category = category;
            this.probability = probability;
        }

        public ClassificationCategory getCategory() {
            return category;
        }

        public double getProbability() {
            return probability;
        }
    }
}
