package com.github.amkaras.tweetus.bayes.algorithm;

import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class NaiveBayesClassificationAlgorithm implements ClassificationAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(NaiveBayesClassificationAlgorithm.class);

    @Override
    public Map<Tweet, ClassificationCategory> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary) {
        return tweets.stream()
                .collect(toMap(identity(), tweet -> chooseMostProbableCategory(tokenize(tweet), dictionary)));
    }

    private ClassificationCategory chooseMostProbableCategory(
            List<String> tokens, Map<ClassificationCategory, Map<String, Long>> dictionary) {

        log.info("Tokens are {}", tokens);

        var laplaceSmoothingParameter = calculateLaplaceSmoothingParameter(dictionary);

        return dictionary.entrySet().stream()
                .map(entry -> entry(entry.getKey(), perCategoryProbability(tokens, entry.getValue(), laplaceSmoothingParameter)))
                .peek(entry -> log.info("Probability for category {} is {}", entry.getKey(), entry.getValue()))
                .max(comparing(Map.Entry::getValue))
                .get()
                .getKey();
    }

    private int calculateLaplaceSmoothingParameter(Map<ClassificationCategory, Map<String, Long>> dictionary) {
        return dictionary.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    private double perCategoryProbability(
            List<String> tokens, Map<String, Long> wordsWithOccurrences, int laplaceSmoothingParameter) {

        double divider = wordsWithOccurrences.size() + laplaceSmoothingParameter;

        return tokens.stream()
                .map(token -> (wordsWithOccurrences.getOrDefault(token, 0L) + 1L) / divider)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private List<String> tokenize(Tweet tweet) {
        return Arrays.stream(tweet.getContent().split("\\s+"))
                .map(String::toLowerCase)
                .map(StringUtils::trimAllWhitespace)
                .filter(removeLinks())
                .filter(removeMentions())
                .filter(removePoliticianNames())
                .filter(atLeastThreeCharacters())
                .map(withoutCommonCharacters())
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

    private Predicate<String> removeLinks() {
        return maybeLink -> !maybeLink.startsWith("http");
    }

    private Predicate<String> removeMentions() {
        return maybeMention -> !maybeMention.startsWith("@");
    }

    private Predicate<String> removePoliticianNames() {
        final Set<String> politicians = Set.of("donald", "trump", "hilary", "clinton");
        return maybePolitician -> politicians.stream().noneMatch(maybePolitician::contains);
    }

    private Predicate<String> atLeastThreeCharacters() {
        return maybeTooShort -> maybeTooShort.length() > 2;
    }
}
