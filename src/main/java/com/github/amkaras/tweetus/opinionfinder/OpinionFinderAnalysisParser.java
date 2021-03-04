package com.github.amkaras.tweetus.opinionfinder;

import com.github.amkaras.tweetus.entity.opinionfinder.DictionaryEntry;
import com.github.amkaras.tweetus.entity.opinionfinder.PolarityClassifier;
import com.github.amkaras.tweetus.entity.opinionfinder.SubjectiveClue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class OpinionFinderAnalysisParser {

    private static final String SPACE = "\\s+";
    private static final String QUOTE = "\"";
    private static final String MPQAPOLARITY = "mpqapolarity=";
    private static final String UNDERSCORE = "_";
    private static final String COMMA = ",";

    public Set<PolarityClassifier> parsePolarityClassifierLines(List<String> lines) {
        return lines.stream()
                .map(toPolarityClassifierScore())
                .collect(groupingBy(identity(), counting()))
                .entrySet().stream()
                .map(toPolarityClassifier())
                .collect(toSet());
    }

    public Set<SubjectiveClue> parseSubjectiveClueLines(List<String> lines) {
        return lines.stream()
                .map(toSubjectiveCluePolarity())
                .collect(groupingBy(identity(), counting()))
                .entrySet().stream()
                .map(toSubjectiveClue())
                .collect(toSet());
    }

    public Set<DictionaryEntry> extractDictionaryEntries(List<String> subjectiveClueLines,
                                                         List<String> polarityClassifierLines,
                                                         String tweetContent) {
        return Stream.concat(
                subjectiveClueLines.stream()
                        .map(subjectiveClueLineToDictionaryEntry(tweetContent)),
                polarityClassifierLines.stream()
                        .map(polarityClassifierLineToDictionaryEntry(tweetContent)))
                .collect(toSet());
    }

    private Function<String, DictionaryEntry> subjectiveClueLineToDictionaryEntry(String tweetContent) {
        return line -> {
            var indexesAndPolarity = line.split(SPACE);
            var indexes = indexesAndPolarity[1].split(COMMA);
            var polarity = indexesAndPolarity[4].replace(MPQAPOLARITY, "").replace(QUOTE, "");
            var beginIndex = indexes[0];
            var endIndex = indexes[1];
            var entry = new DictionaryEntry();
            entry.setToken(tweetContent.substring(parseInt(beginIndex), parseInt(endIndex)));
            entry.setPolarity(polarity);
            return entry;
        };
    }

    private Function<String, DictionaryEntry> polarityClassifierLineToDictionaryEntry(String tweetContent) {
        return line -> {
            var indexesAndPolarity = line.split(SPACE);
            var indexes = indexesAndPolarity[0].split(UNDERSCORE);
            var polarity = indexesAndPolarity[1];
            var beginIndex = indexes[2];
            var endIndex = indexes[3];
            var entry = new DictionaryEntry();
            entry.setToken(tweetContent.substring(parseInt(beginIndex), parseInt(endIndex)));
            entry.setPolarity(polarity);
            return entry;
        };
    }

    private Function<String, String> toPolarityClassifierScore() {
        return line -> line.split(SPACE)[1];
    }

    private Function<Map.Entry<String, Long>, PolarityClassifier> toPolarityClassifier() {
        return entry -> {
            var polarityClassifier = new PolarityClassifier();
            polarityClassifier.setScore(entry.getKey());
            polarityClassifier.setCount(entry.getValue().intValue());
            return polarityClassifier;
        };
    }

    private Function<String, String> toSubjectiveCluePolarity() {
        return line -> line.split(SPACE)[4].replace(MPQAPOLARITY, "").replace(QUOTE, "");
    }

    private Function<Map.Entry<String, Long>, SubjectiveClue> toSubjectiveClue() {
        return entry -> {
            var subjectiveClue = new SubjectiveClue();
            subjectiveClue.setPolarity(entry.getKey());
            subjectiveClue.setCount(entry.getValue().intValue());
            return subjectiveClue;
        };
    }
}
