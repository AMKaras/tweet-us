package com.github.amkaras.tweetus.common.analysis;

import com.github.amkaras.tweetus.common.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.common.logic.WeightedClassificationCategorySelector;
import com.github.amkaras.tweetus.common.model.Algorithm;
import com.github.amkaras.tweetus.common.model.BinaryClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.configuration.FeatureToggles;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.opinionfinder.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.amkaras.tweetus.common.model.ClassificationType.BINARY;
import static java.util.stream.Collectors.toList;

public abstract class AnalysisExecutor {

    public static final Logger log = LoggerFactory.getLogger(AnalysisExecutor.class);

    private final TweetService tweetService;
    private final OpinionFinderAnalysisService opinionFinderAnalysisService;
    private final WeightedClassificationCategorySelector categorySelector;
    protected final FeatureToggles featureToggles;

    protected ClassificationAlgorithm algorithm;
    protected List<Tweet> trainingSet;
    protected List<OpinionFinderAnalysis> trainingSetAnalyses;
    protected List<Tweet> testSet;
    protected List<OpinionFinderAnalysis> testSetAnalyses;

    protected AnalysisExecutor(TweetService tweetService, OpinionFinderAnalysisService opinionFinderAnalysisService,
                               FeatureToggles featureToggles) {
        this.tweetService = tweetService;
        this.opinionFinderAnalysisService = opinionFinderAnalysisService;
        this.featureToggles = featureToggles;
        this.categorySelector = new WeightedClassificationCategorySelector();
    }

    public abstract void analyze();

    boolean comparativeAnalysisEnabled() {
        return featureToggles.isBayesAnalysisExecutorEnabled() && featureToggles.isKnnAnalysisExecutorEnabled();
    }

    void prepareTrainingSet(int size) {
        log.info("Preparing training set");
        tweetService.markAllAsNotBelongingToTrainingSet();
        tweetService.markAsBelongingToTrainingSet(size);
        log.info("Fetching tweets from training set");
        this.trainingSet = filterOutDuplicatedContents(tweetService.findBelongingToTrainingSet(size));
        log.info("Fetched {} tweets", trainingSet.size());
        fetchAnalysesForTrainingSet();
    }

    void prepareTestSet(int size) {
        log.info("Fetching tweets to classify");
        this.testSet = filterOutDuplicatedContents(tweetService.findAnalyzedNotBelongingToTrainingSet(size));
        log.info("Fetched {} tweets", testSet.size());
        fetchAnalysesForTestSet();
    }

    AnalysisResults compareResults(Map<Tweet, Optional<ClassificationCategory>> algorithmClassifications,
                                   ClassificationType type, Algorithm algorithm) {

        double consistentClassificationCount = 0.0;
        double totalClassificationsCount = 0.0;

        for (Map.Entry<Tweet, Optional<ClassificationCategory>> entry : algorithmClassifications.entrySet()) {
            Tweet tweet = entry.getKey();
            Optional<ClassificationCategory> maybeAlgorithmClassification = entry.getValue();
            if (maybeAlgorithmClassification.isEmpty()) {
                log.debug("Cannot select category using {} algorithm for tweet {}", algorithm, tweet.getId());
                continue;
            }
            OpinionFinderAnalysis ofAnalysis = chooseByTweetId(testSetAnalyses, tweet.getId());
            var maybeOfClassification = categorySelector.select(
                    ofAnalysis.getSubjectiveClues(), ofAnalysis.getPolarityClassifiers());
            if (maybeOfClassification.isEmpty()) {
                log.debug("Cannot select category using OpinionFinder for tweet {}", tweet.getId());
                continue;
            }
            var ofClassification = type == BINARY ?
                    BinaryClassificationCategory.map(maybeOfClassification.get()) : maybeOfClassification.get();
            log.debug("Tweet {} classified by algorithm {} as {}. OpinionFinder classified it as {}",
                    tweet.getId(), algorithm, maybeAlgorithmClassification, ofClassification);
            if (maybeAlgorithmClassification.get() == ofClassification) {
                consistentClassificationCount += 1.0;
            }
            totalClassificationsCount += 1.0;
        }
        log.info("{} classifications were consistent for {}/{} analyses, which gives {}% accuracy", algorithm,
                consistentClassificationCount, totalClassificationsCount,
                (consistentClassificationCount * 100.0) / totalClassificationsCount);
        return new AnalysisResults(consistentClassificationCount, totalClassificationsCount);
    }

    private void fetchAnalysesForTrainingSet() {
        log.info("Fetching analyses for training set tweets");
        this.trainingSetAnalyses = fetchAnalysesForTweets(trainingSet);
        log.info("Fetched {} analyses", trainingSetAnalyses.size());
        this.trainingSet = filterOutNotClassifiableWithOpinionFinder(this.trainingSet, this.trainingSetAnalyses);
        this.trainingSetAnalyses = filterOutWhenTweetIdNotPresent(this.trainingSetAnalyses, this.trainingSet);
    }

    private void fetchAnalysesForTestSet() {
        log.info("Fetching analyses for test set tweets");
        this.testSetAnalyses = fetchAnalysesForTweets(testSet);
        log.info("Fetched {} analyses", testSetAnalyses.size());
        this.testSet = filterOutNotClassifiableWithOpinionFinder(this.testSet, this.testSetAnalyses);
        this.testSetAnalyses = filterOutWhenTweetIdNotPresent(this.testSetAnalyses, this.testSet);
    }

    private List<OpinionFinderAnalysis> fetchAnalysesForTweets(Collection<Tweet> tweets) {
        var ids = tweets.stream()
                .map(Tweet::getId)
                .collect(toList());
        return opinionFinderAnalysisService.findByEntityIds(ids);
    }

    private OpinionFinderAnalysis chooseByTweetId(List<OpinionFinderAnalysis> analyses, String tweetId) {
        return analyses.stream()
                .filter(analysis -> analysis.getEntityId().equals(tweetId))
                .findFirst()
                .get();
    }

    private List<Tweet> filterOutDuplicatedContents(List<Tweet> tweets) {
        final List<Tweet> filtered = new ArrayList<>();
        for (Tweet tweet : tweets) {
            if (filtered.stream().noneMatch(duplicatedContent(tweet))) {
                filtered.add(tweet);
            } else {
                log.debug("Duplicated content: {}", tweet.getContent());
            }
        }
        return filtered;
    }

    private List<Tweet> filterOutNotClassifiableWithOpinionFinder(
            List<Tweet> tweets, List<OpinionFinderAnalysis> analyses) {
        return tweets.stream()
                .filter(tweet -> {
                    OpinionFinderAnalysis analysis = chooseByTweetId(analyses, tweet.getId());
                    return categorySelector
                            .select(analysis.getSubjectiveClues(), analysis.getPolarityClassifiers()).isPresent();
                })
                .collect(toList());
    }

    private List<OpinionFinderAnalysis> filterOutWhenTweetIdNotPresent(
            List<OpinionFinderAnalysis> analyses, List<Tweet> tweets) {
        return analyses.stream()
                .filter(analysis -> tweets.stream().anyMatch(tweet -> analysis.getEntityId().equals(tweet.getId())))
                .collect(toList());
    }

    private Predicate<Tweet> duplicatedContent(Tweet tweet) {
        return maybeDuplicatedContentTweet -> maybeDuplicatedContentTweet.getContent().toLowerCase()
                .equals(tweet.getContent().toLowerCase());
    }
}
