package com.github.amkaras.tweetus.common.util;

import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Function;

import static com.github.amkaras.tweetus.common.util.FiltersFactory.atLeastThreeCharacters;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.onlyCharacters;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.withoutCommonCharacters;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.withoutCommonWords;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.withoutMentions;
import static com.github.amkaras.tweetus.common.util.FiltersFactory.withoutPoliticianNames;
import static java.util.stream.Collectors.toList;

public class Tokenizer {

    public static final String WHITESPACES = "\\s+";
    public static final String WHITESPACE = " ";

    private Tokenizer() {
    }

    public static List<String> prepareTokens(Tweet tweet) {
        return filters().apply(List.of(tweet.getContent().split(WHITESPACES)));
    }

    public static List<String> prepareTokens(String content) {
        return filters().apply(List.of(content.split(WHITESPACES)));
    }

    public static List<String> prepareTokens(List<String> tokens) {
        return filters().apply(tokens);
    }

    public static List<String> splitOnly(String content) {
        return List.of(content.split(WHITESPACES));
    }

    public static String join(List<String> tokens) {
        return String.join(WHITESPACE, tokens);
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
