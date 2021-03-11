package com.github.amkaras.tweetus.bayes.utils;

import com.github.amkaras.tweetus.twitter.entity.Tweet;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Function;

import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.atLeastThreeCharacters;
import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.onlyCharacters;
import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.withoutCommonCharacters;
import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.withoutCommonWords;
import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.withoutMentions;
import static com.github.amkaras.tweetus.bayes.utils.FiltersFactory.withoutPoliticianNames;
import static java.util.stream.Collectors.toList;

public class Tokenizer {

    private static final String WHITESPACES = "\\s+";

    private Tokenizer() {
    }

    public static List<String> prepareTokens(Tweet tweet) {
        return filters().apply(List.of(tweet.getContent().split(WHITESPACES)));
    }

    public static List<String> prepareTokens(List<String> tokens) {
        return filters().apply(tokens);
    }

    private static Function<List<String>, List<String>> filters() {
        return tokens -> tokens.stream()
                .map(String::toLowerCase)
                .map(StringUtils::trimAllWhitespace)
                .filter(withoutMentions())
                .filter(withoutPoliticianNames())
                .map(withoutCommonCharacters())
                .filter(atLeastThreeCharacters())
                .filter(onlyCharacters())
                .filter(withoutCommonWords())
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .collect(toList());
    }
}
