package com.github.amkaras.tweetus.bayes.analysis;

import com.github.amkaras.tweetus.bayes.DictionaryBuilder;
import com.github.amkaras.tweetus.bayes.WeightedClassificationCategorySelector;
import com.github.amkaras.tweetus.bayes.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.bayes.algorithm.NaiveBayesClassificationAlgorithm;
import com.github.amkaras.tweetus.bayes.category.BinaryClassificationCategory;
import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.Tweet;
import com.github.amkaras.tweetus.entity.opinionfinder.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

import static com.github.amkaras.tweetus.bayes.category.ClassificationType.BINARY;
import static java.util.stream.Collectors.toList;

@Component
public class LearningNaiveBayesTweetsAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(LearningNaiveBayesTweetsAnalyzer.class);

    private final TweetService tweetService;
    private final OpinionFinderAnalysisService opinionFinderAnalysisService;

    @Autowired
    public LearningNaiveBayesTweetsAnalyzer(
            TweetService tweetService, OpinionFinderAnalysisService opinionFinderAnalysisService) {
        this.tweetService = tweetService;
        this.opinionFinderAnalysisService = opinionFinderAnalysisService;
    }

    @PostConstruct
    public void analyze() {

        log.info("Fetching tweets from training set");
        var trainingSetTweets = tweetService.findBelongingToTrainingSet(1000);
        log.info("Fetched {} tweets", trainingSetTweets.size());
        log.info("Fetching analysis for training set tweets");
        var opinionFinderAnalysis = opinionFinderAnalysisService.findByEntityIds(
                trainingSetTweets.stream()
                        .map(Tweet::getId)
                        .collect(toList()));
        log.info("Fetched {} analysis", opinionFinderAnalysis.size());

        ClassificationAlgorithm algorithm = new NaiveBayesClassificationAlgorithm();
        DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
        log.info("Processing analysis to dictionary");
        var dictionary = dictionaryBuilder.build(opinionFinderAnalysis, BINARY);
        log.info("Processed. Dictionary is {}", dictionary);

        log.info("Fetching tweets to classify");
        var tweetsToBeClassified = tweetService.findAnalyzedNotBelongingToTrainingSet(100);
        log.info("Fetched {} tweets. Classifying...", tweetsToBeClassified.size());
        var classificationResult = algorithm.classify(tweetsToBeClassified, dictionary);
        log.info("Classified {} tweets. Fetching analysis for comparison", classificationResult.size());
        var classifiedTweetsAnalysis = opinionFinderAnalysisService.findByEntityIds(
                classificationResult.keySet().stream()
                        .map(Tweet::getId)
                        .collect(toList()));
        log.info("Fetched {} analysis", classifiedTweetsAnalysis.size());

        WeightedClassificationCategorySelector categorySelector = new WeightedClassificationCategorySelector();
        double consistentClassificationCount = 0.0;
        double totalClassificationsCount = 0.0;

        for (Map.Entry<Tweet, Optional<ClassificationCategory>> entry : classificationResult.entrySet()) {
            Tweet tweet = entry.getKey();
            Optional<ClassificationCategory> category = entry.getValue();
            if (category.isEmpty()) {
                log.info("Cannot select category for tweet {}: {}", tweet.getId(), tweet.getContent());
                continue;
            }
            OpinionFinderAnalysis opinionFinderAnalysisForClassifiedTweet = classifiedTweetsAnalysis.stream()
                    .filter(analysis -> analysis.getEntityId().equals(tweet.getId()))
                    .findFirst()
                    .get();
            var opinionFinderClassification = BinaryClassificationCategory.map(
                    categorySelector.select(
                            opinionFinderAnalysisForClassifiedTweet.getSubjectiveClues(),
                            opinionFinderAnalysisForClassifiedTweet.getPolarityClassifiers()));
            log.info("Tweet {} [{}] classified by Naive Bayes algorithm as {}. OpinionFinder classified it as {}",
                    tweet.getId(), tweet.getContent(), category, opinionFinderClassification);
            if (category.get() == opinionFinderClassification) {
                consistentClassificationCount += 1.0;
            }
            totalClassificationsCount += 1.0;
        }
        log.info("Classifications were consistent for {}/{} analysis, which gives {}% accuracy",
                (int) consistentClassificationCount, (int) totalClassificationsCount,
                (consistentClassificationCount * 100.0) / totalClassificationsCount);
    }
}
