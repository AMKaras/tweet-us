package com.github.amkaras.tweetus.bayes.algorithm;

import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class NaiveBayesClassificationAlgorithm implements ClassificationAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassificationAlgorithm.class);

    @Override
    public Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary) {
        return tweets.stream()
                .map(tweet -> entry(tweet, tokenize(tweet)))
                .filter(tweetWithTokens -> tweetWithTokens.getValue().size() > 2)
                .collect(toMap(Map.Entry::getKey,
                        tweetWithTokens -> chooseMostProbableCategory(tweetWithTokens.getValue(), dictionary)));
    }

    private Optional<ClassificationCategory> chooseMostProbableCategory(
            List<String> tokens, Map<ClassificationCategory, Map<String, Long>> dictionary) {

        log.info("Tokens are {}", tokens);

        var laplaceSmoothingParameter = calculateLaplaceSmoothingParameter(dictionary);

        var categoriesByProbabilty = dictionary.entrySet().stream()
                .map(entry -> new CategoryWithProbabilityDecorator(entry.getKey(), perCategoryProbability(tokens, entry.getValue(), laplaceSmoothingParameter)))
                .peek(entry -> log.info("Probability for category {} is {}", entry.getCategory(), entry.getProbability()))
                .sorted(comparing(CategoryWithProbabilityDecorator::getProbability, reverseOrder()))
                .collect(toList());
        return categoriesByProbabilty.get(0).getProbability() == categoriesByProbabilty.get(1).getProbability() ?
                Optional.empty() : Optional.of(categoriesByProbabilty.get(0).getCategory());
    }

    private int calculateLaplaceSmoothingParameter(Map<ClassificationCategory, Map<String, Long>> dictionary) {
        return dictionary.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    private double perCategoryProbability(
            List<String> tokens, Map<String, Long> wordsWithOccurrences, int laplaceSmoothingParameter) {

        double divider = wordsWithOccurrences.values().stream()
                .mapToInt(Long::intValue)
                .sum()
                + laplaceSmoothingParameter;

        return tokens.stream()
                .map(token -> (wordsWithOccurrences.getOrDefault(token, 0L) + 1L) / divider)
                .mapToDouble(Double::doubleValue)
                .reduce(1, (a, b) -> a * b);
    }

    private List<String> tokenize(Tweet tweet) {
        return Arrays.stream(tweet.getContent().split("\\s+"))
                .map(String::toLowerCase)
                .map(StringUtils::trimAllWhitespace)
                .filter(removeMentions())
                .filter(removePoliticianNames())
                .map(withoutCommonCharacters())
                .filter(atLeastThreeCharacters())
                .filter(onlyCharacters())
                .collect(toList());
    }

    private Function<String, String> withoutCommonCharacters() {
        return word -> word
                .replace(".", "")
                .replace("…", "")
                .replace(",", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("?", "")
                .replace("!", "")
                .replace("#", "")
                .replace("@", "")
                .replace("[", "")
                .replace("]", "")
                .replace("(", "")
                .replace(")", "")
                .replace("*", "")
                .replace(":", "")
                .replace("'", "");
    }

    private Predicate<String> removeMentions() {
        return maybeMention -> !maybeMention.startsWith("@");
    }

    private Predicate<String> removePoliticianNames() {
        final Set<String> politicians = Set.of("donald", "trump", "hilary", "clinton");
        return maybePolitician -> politicians.stream().noneMatch(maybePolitician::contains);
    }

    private Predicate<String> onlyCharacters() {
        return maybeNotCharacters -> maybeNotCharacters.matches("[a-zA-Z]+");
    }

    private Predicate<String> atLeastThreeCharacters() {
        return maybeTooShort -> maybeTooShort.length() > 2;
    }

    private static final class CategoryWithProbabilityDecorator {

        private ClassificationCategory category;
        private double probability;

        public CategoryWithProbabilityDecorator(ClassificationCategory category, double probability) {
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
