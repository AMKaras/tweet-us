package com.github.amkaras.tweetus.common.util;

import com.github.amkaras.tweetus.external.twitter.entity.Tweet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class FiltersFactory {

    private FiltersFactory() {
    }

    public static Predicate<Map.Entry<Tweet, List<String>>> atLeastThreeTokens() {
        return maybeNotEnoughTokens -> maybeNotEnoughTokens.getValue().size() > 2;
    }

    public static Predicate<String> withoutMentions() {
        return maybeMention -> !maybeMention.startsWith("@");
    }

    public static Predicate<String> withoutPoliticianNames() {
        final Set<String> politicians = Set.of("donald", "trump", "hilary", "hillary", "clinton");
        return maybePolitician -> politicians.stream().noneMatch(maybePolitician::contains);
    }

    public static Predicate<String> withoutCommonWords() {
        final Set<String> commonWords = Set.of("the", "and");
        return maybeCommonWord -> commonWords.stream().noneMatch(maybeCommonWord::contains);
    }

    public static Predicate<String> onlyCharacters() {
        return maybeOnlyCharacters -> maybeOnlyCharacters.matches("[a-zA-Z]+");
    }

    public static Predicate<String> atLeastThreeCharacters() {
        return maybeTooShort -> maybeTooShort.length() > 2;
    }

    public static Function<String, String> withoutCommonCharacters() {
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
}
