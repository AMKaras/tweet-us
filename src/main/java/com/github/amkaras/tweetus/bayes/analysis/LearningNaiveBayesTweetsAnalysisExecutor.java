package com.github.amkaras.tweetus.bayes.analysis;

import com.github.amkaras.tweetus.bayes.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.bayes.algorithm.NaiveBayesClassificationAlgorithm;
import com.github.amkaras.tweetus.bayes.category.BinaryClassificationCategory;
import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.opinionfinder.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.twitter.entity.Tweet;
import com.github.amkaras.tweetus.twitter.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.amkaras.tweetus.bayes.category.ClassificationType.BINARY;
import static java.util.stream.Collectors.toList;

@Component
public class LearningNaiveBayesTweetsAnalysisExecutor {

    private static final Logger log = LoggerFactory.getLogger(LearningNaiveBayesTweetsAnalysisExecutor.class);

    private final TweetService tweetService;
    private final OpinionFinderAnalysisService opinionFinderAnalysisService;
    private final WeightedClassificationCategorySelector categorySelector;
    private final ClassificationAlgorithm algorithm;
    private final DictionaryBuilder dictionaryBuilder;

    @Autowired
    public LearningNaiveBayesTweetsAnalysisExecutor(
            TweetService tweetService, OpinionFinderAnalysisService opinionFinderAnalysisService) {
        this.tweetService = tweetService;
        this.opinionFinderAnalysisService = opinionFinderAnalysisService;
        final StanfordLemmatizerClient lemmatizerClient = new StanfordLemmatizerClient();
        this.categorySelector = new WeightedClassificationCategorySelector();
        this.algorithm = new NaiveBayesClassificationAlgorithm(lemmatizerClient);
        this.dictionaryBuilder = new DictionaryBuilder(lemmatizerClient);
    }

    //    @PostConstruct
    public void analyze() {

        log.info("Fetching tweets from training set");
        var tweetsFromTrainingSet = tweetService.findBelongingToTrainingSet(18000);
        tweetsFromTrainingSet = filterOutDuplicatedContents(tweetsFromTrainingSet);
        log.info("Fetched {} tweets", tweetsFromTrainingSet.size());
        log.info("Fetching analysis for training set tweets");
        var ofAnalysesForTrainingSet = fetchAnalysesForTweets(tweetsFromTrainingSet);
        log.info("Fetched {} analysis", ofAnalysesForTrainingSet.size());

        log.info("Processing analysis to dictionary");
        var dictionary = dictionaryBuilder.build(ofAnalysesForTrainingSet, BINARY, true);
        log.info("Processed. Dictionary is {}", dictionary);
        var nonLemmatizedDictionary = dictionaryBuilder.build(ofAnalysesForTrainingSet, BINARY, false);
        log.info("Processed. Non lemmatized ictionary is {}", nonLemmatizedDictionary);

        log.info("Fetching tweets to classify");
        var tweetsFromTestSet = tweetService.findAnalyzedNotBelongingToTrainingSet(3000);
        tweetsFromTestSet = filterOutDuplicatedContents(tweetsFromTestSet);
        log.info("Fetched {} tweets. Classifying...", tweetsFromTestSet.size());
        log.info("With lemmatization enabled:");
        var bayesClassifications = algorithm.classify(tweetsFromTestSet, dictionary, true);
        log.info("With lemmatization disabled:");
        var nonLemmatizedBayesClassifications = algorithm.classify(tweetsFromTestSet, nonLemmatizedDictionary, false);
        log.info("Classified {} tweets. Fetching analysis for comparison", bayesClassifications.size());
        var ofAnalysesForTestSet = fetchAnalysesForTweets(tweetsFromTestSet);
        log.info("Fetched {} analysis", ofAnalysesForTestSet.size());

        log.info("Lemmatized analyses results:");
        compareResults(bayesClassifications, ofAnalysesForTestSet);
        log.info("Non lemmatized analyses results:");
        compareResults(nonLemmatizedBayesClassifications, ofAnalysesForTestSet);
    }

    private void compareResults(Map<Tweet, Optional<ClassificationCategory>> bayesClassifications,
                                List<OpinionFinderAnalysis> opinionFinderAnalyses) {

        double consistentClassificationCount = 0.0;
        double totalClassificationsCount = 0.0;

        for (Map.Entry<Tweet, Optional<ClassificationCategory>> entry : bayesClassifications.entrySet()) {
            Tweet tweet = entry.getKey();
            Optional<ClassificationCategory> bayesClassification = entry.getValue();
            if (bayesClassification.isEmpty()) {
                log.info("Cannot select category for tweet {}", tweet.getId());
                continue;
            }
            OpinionFinderAnalysis ofAnalysis = chooseByTweetId(opinionFinderAnalyses, tweet.getId());
            var ofClassification = BinaryClassificationCategory.map(categorySelector.select(
                    ofAnalysis.getSubjectiveClues(), ofAnalysis.getPolarityClassifiers()));
            log.info("Tweet {} classified by Naive Bayes algorithm as {}. OpinionFinder classified it as {}",
                    tweet.getId(), bayesClassification, ofClassification);
            if (bayesClassification.get() == ofClassification) {
                consistentClassificationCount += 1.0;
            }
            totalClassificationsCount += 1.0;
        }
        log.info("Classifications were consistent for {}/{} analysis, which gives {}% accuracy",
                consistentClassificationCount, totalClassificationsCount,
                (consistentClassificationCount * 100.0) / totalClassificationsCount);
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
            }
        }
        return filtered;
    }

    private Predicate<Tweet> duplicatedContent(Tweet tweet) {
        return maybeDuplicatedContentTweet -> maybeDuplicatedContentTweet.getContent().equals(tweet.getContent());
    }
}
