package com.github.amkaras.tweetus.common.analysis;

import com.github.amkaras.tweetus.common.algorithm.bayes.DictionaryBuilder;
import com.github.amkaras.tweetus.common.algorithm.bayes.NaiveBayesClassificationAlgorithm;
import com.github.amkaras.tweetus.common.model.Algorithm;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.configuration.FeatureToggles;
import com.github.amkaras.tweetus.external.opinionfinder.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.external.stanfordnlp.StanfordLemmatizerClient;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.service.TweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Component
public class LearningNaiveBayesTweetsAnalysisExecutor extends AnalysisExecutor {

    private final DictionaryBuilder dictionaryBuilder;
    private Map<ClassificationCategory, Map<String, Long>> dictionary;
    private Map<ClassificationCategory, Map<String, Long>> nonLemmatizedDictionary;
    private Map<Tweet, Optional<ClassificationCategory>> bayesClassifications;
    private Map<Tweet, Optional<ClassificationCategory>> nonLemmatizedBayesClassifications;

    @Autowired
    public LearningNaiveBayesTweetsAnalysisExecutor(TweetService tweetService,
                                                    OpinionFinderAnalysisService opinionFinderAnalysisService,
                                                    FeatureToggles featureToggles) {
        super(tweetService, opinionFinderAnalysisService, featureToggles);
        final StanfordLemmatizerClient lemmatizerClient = featureToggles.isStanfordNlpClasspathConfigured() ?
                StanfordLemmatizerClient.createConfigured() : StanfordLemmatizerClient.createNotConfigured();
        this.algorithm = new NaiveBayesClassificationAlgorithm(lemmatizerClient);
        this.dictionaryBuilder = new DictionaryBuilder(lemmatizerClient);
    }

    @PostConstruct
    @Override
    public void analyze() {

        if (!featureToggles.isBayesAnalysisExecutorEnabled() || comparativeAnalysisEnabled()) {
            return;
        }

        ClassificationType classificationType = featureToggles.getBayesAnalysisClassificationType();
        boolean lemmatizedModeEnabled = featureToggles.isBayesAnalysisLemmatizedModeEnabled();
        boolean nonLemmatizedModeEnabled = featureToggles.isBayesAnalysisNonLemmatizedModeEnabled();

        prepareTrainingSet(featureToggles.getBayesAnalysisTrainingSetSize());

        log.info("Processing analyses to dictionary");

        if (lemmatizedModeEnabled) {
            dictionary = dictionaryBuilder.build(trainingSetAnalyses, classificationType, true);
            log.info("Processed. Dictionary is {}", dictionary);
        }
        if (nonLemmatizedModeEnabled) {
            nonLemmatizedDictionary = dictionaryBuilder.build(trainingSetAnalyses, classificationType, false);
            log.info("Processed. Non lemmatized dictionary is {}", nonLemmatizedDictionary);
        }

        prepareTestSet(featureToggles.getBayesAnalysisTestSetSize());

        if (lemmatizedModeEnabled) {
            log.info("Classifying with lemmatization enabled:");
            bayesClassifications = algorithm.classify(testSet, dictionary, true);
        }
        if (nonLemmatizedModeEnabled) {
            log.info("Classifying with lemmatization disabled:");
            nonLemmatizedBayesClassifications = algorithm.classify(testSet, nonLemmatizedDictionary, false);
        }

        if (lemmatizedModeEnabled) {
            log.info("Lemmatized analyses results:");
            compareResults(bayesClassifications, classificationType, Algorithm.BAYES);
        }
        if (nonLemmatizedModeEnabled) {
            log.info("Non lemmatized analyses results:");
            compareResults(nonLemmatizedBayesClassifications, classificationType, Algorithm.BAYES);
        }
    }
}
