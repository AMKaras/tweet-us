package com.github.amkaras.tweetus.common.algorithm.knn;

import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.logic.WeightedClassificationCategorySelector;
import com.github.amkaras.tweetus.common.model.BinaryClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.amkaras.tweetus.common.algorithm.knn.model.Document.Builder.documentBuilder;
import static com.github.amkaras.tweetus.common.model.ClassificationType.DIFFERENTIAL;
import static com.github.amkaras.tweetus.common.util.Tokenizer.prepareTokens;
import static java.lang.String.join;
import static java.util.function.Function.identity;

public class DocumentsBuilder {

    private static final String SPACE = " ";
    private final WeightedClassificationCategorySelector categorySelector = new WeightedClassificationCategorySelector();
    private final StanfordLemmatizerClient lemmatizerClient;

    public DocumentsBuilder(StanfordLemmatizerClient lemmatizerClient) {
        this.lemmatizerClient = lemmatizerClient;
    }

    public Set<Document> build(List<Tweet> tweets, List<OpinionFinderAnalysis> analyses,
                               ClassificationType classificationType, boolean lemmatizationEnabled) {
        Map<Tweet, OpinionFinderAnalysis> matchedAnalyses = tweets.stream()
                .collect(Collectors.toMap(identity(), tweet -> analyses.stream()
                        .filter(analysis -> analysis.getEntityId().equals(tweet.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("There's no analysis matching tweet id"))));
        Set<Document> documents = new HashSet<>();
        for (Map.Entry<Tweet, OpinionFinderAnalysis> analysisEntry : matchedAnalyses.entrySet()) {
            Optional<? extends ClassificationCategory> maybeCategory = selectCategory(classificationType, analysisEntry.getValue());
            maybeCategory.ifPresent(classificationCategory -> documents.add(
                    documentBuilder()
                            .withContent(getContent(analysisEntry.getKey(), lemmatizationEnabled))
                            .withCategory(classificationCategory)
                            .build()));
        }
        return documents;
    }

    private String getContent(Tweet tweet, boolean lemmatizationEnabled) {
        String content = join(SPACE, prepareTokens(tweet));
        return lemmatizationEnabled ? join(SPACE, lemmatizerClient.lemmatize(content)) : content;
    }

    private Optional<? extends ClassificationCategory> selectCategory(ClassificationType classificationType, OpinionFinderAnalysis analysis) {
        var maybeCategory = categorySelector
                .select(analysis.getSubjectiveClues(), analysis.getPolarityClassifiers());
        if (maybeCategory.isEmpty()) {
            return Optional.empty();
        }
        return classificationType == DIFFERENTIAL ?
                maybeCategory : Optional.of(BinaryClassificationCategory.map(maybeCategory.get()));
    }
}
