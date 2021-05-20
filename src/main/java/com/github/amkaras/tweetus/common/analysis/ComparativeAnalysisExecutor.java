package com.github.amkaras.tweetus.common.analysis;

import com.github.amkaras.tweetus.common.algorithm.ClassificationAlgorithm;
import com.github.amkaras.tweetus.common.algorithm.bayes.DictionaryBuilder;
import com.github.amkaras.tweetus.common.algorithm.bayes.NaiveBayesClassificationAlgorithm;
import com.github.amkaras.tweetus.common.algorithm.knn.DocumentsBuilder;
import com.github.amkaras.tweetus.common.algorithm.knn.KNNClassificationAlgorithm;
import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ComparativeAnalysisExecutor extends AnalysisExecutor {

    private final DictionaryBuilder dictionaryBuilder;
    private final DocumentsBuilder documentsBuilder;

    private final ClassificationAlgorithm bayesAlgorithm;
    private Map<ClassificationCategory, Map<String, Long>> dictionary;
    private Map<ClassificationCategory, Map<String, Long>> nonLemmatizedDictionary;
    private Map<Tweet, Optional<ClassificationCategory>> bayesClassifications;
    private Map<Tweet, Optional<ClassificationCategory>> nonLemmatizedBayesClassifications;

    private final ClassificationAlgorithm knnAlgorithm;
    private Set<Document> documents;
    private Set<Document> nonLemmatizedDocuments;
    private List<Map<Tweet, Optional<ClassificationCategory>>> knnClassifications = new ArrayList<>();
    private List<Map<Tweet, Optional<ClassificationCategory>>> nonLemmatizedKNNClassifications = new ArrayList<>();

    @Autowired
    public ComparativeAnalysisExecutor(TweetService tweetService,
                                       OpinionFinderAnalysisService opinionFinderAnalysisService,
                                       FeatureToggles featureToggles) {
        super(tweetService, opinionFinderAnalysisService, featureToggles);
        final StanfordLemmatizerClient lemmatizerClient = featureToggles.isStanfordNlpClasspathConfigured() ?
                StanfordLemmatizerClient.createConfigured() : StanfordLemmatizerClient.createNotConfigured();
        this.dictionaryBuilder = new DictionaryBuilder(lemmatizerClient);
        this.documentsBuilder = new DocumentsBuilder(lemmatizerClient);
        this.bayesAlgorithm = new NaiveBayesClassificationAlgorithm(lemmatizerClient);
        this.knnAlgorithm = new KNNClassificationAlgorithm(lemmatizerClient);
    }

    @PostConstruct
    @Override
    public void analyze() {

        if (!comparativeAnalysisEnabled()) {
            return;
        }

        if (featureToggles.getBayesAnalysisTrainingSetSize() != featureToggles.getKnnAnalysisTrainingSetSize()
                || featureToggles.getBayesAnalysisTestSetSize() != featureToggles.getKnnAnalysisTestSetSize()
                || featureToggles.getBayesAnalysisClassificationType() != featureToggles.getKnnAnalysisClassificationType()
                || featureToggles.isBayesAnalysisLemmatizedModeEnabled() != featureToggles.isKnnAnalysisLemmatizedModeEnabled()
                || featureToggles.isBayesAnalysisNonLemmatizedModeEnabled() != featureToggles.isKnnAnalysisNonLemmatizedModeEnabled()) {
            throw new IllegalStateException("Configurations not consistent in comparative analysis mode!");
        }

        ClassificationType classificationType = featureToggles.getBayesAnalysisClassificationType();
        boolean lemmatizedModeEnabled = featureToggles.isBayesAnalysisLemmatizedModeEnabled();
        boolean nonLemmatizedModeEnabled = featureToggles.isBayesAnalysisNonLemmatizedModeEnabled();

        prepareTrainingSet(featureToggles.getBayesAnalysisTrainingSetSize());
        prepareTestSet(featureToggles.getBayesAnalysisTestSetSize());

        log.info("Processing analyses to dictionary for Bayes and documents for KNN");

        if (lemmatizedModeEnabled) {
            dictionary = dictionaryBuilder.build(trainingSetAnalyses, classificationType, true);
            log.info("Processed. Dictionary is {}", dictionary);
            documents = documentsBuilder.build(trainingSet, trainingSetAnalyses, classificationType, true);
            log.info("Processed. Documents are {}", documents);
        }
        if (nonLemmatizedModeEnabled) {
            nonLemmatizedDictionary = dictionaryBuilder.build(trainingSetAnalyses, classificationType, false);
            log.info("Processed. Non lemmatized dictionary is {}", nonLemmatizedDictionary);
            nonLemmatizedDocuments = documentsBuilder.build(trainingSet, trainingSetAnalyses, classificationType, false);
            log.info("Processed. Non lemmatized documents are {}", nonLemmatizedDocuments);
        }

        if (lemmatizedModeEnabled) {
            log.info("Classifying using Bayes with lemmatization enabled:");
            bayesClassifications = bayesAlgorithm.classify(testSet, dictionary, true);
            featureToggles.getKnnAnalysisParameterK().forEach(k -> {
                log.info("Classifying using KNN with k = {} and lemmatization enabled:", k);
                knnClassifications.add(knnAlgorithm.classify(testSet, documents, true, k));
            });
        }
        if (nonLemmatizedModeEnabled) {
            log.info("Classifying using Bayes with lemmatization disabled:");
            nonLemmatizedBayesClassifications = bayesAlgorithm.classify(testSet, nonLemmatizedDictionary, false);
            featureToggles.getKnnAnalysisParameterK().forEach(k -> {
                log.info("Classifying using KNN with k = {} and lemmatization disabled:", k);
                nonLemmatizedKNNClassifications.add(knnAlgorithm.classify(testSet, documents, false, k));
            });
        }

        if (lemmatizedModeEnabled) {
            log.info("Bayes lemmatized analyses results:");
            compareResults(bayesClassifications, classificationType, Algorithm.BAYES);
            knnClassifications.forEach(classification -> {
                log.info("KNN lemmatized analyses results for k = {}:",
                        featureToggles.getKnnAnalysisParameterK().get(knnClassifications.indexOf(classification)));
                compareResults(classification, classificationType, Algorithm.KNN);
            });
        }
        if (nonLemmatizedModeEnabled) {
            log.info("Bayes non lemmatized analyses results:");
            compareResults(nonLemmatizedBayesClassifications, classificationType, Algorithm.BAYES);
            nonLemmatizedKNNClassifications.forEach(classification -> {
                log.info("KNN non lemmatized analyses results for k = {}:",
                        featureToggles.getKnnAnalysisParameterK().get(nonLemmatizedKNNClassifications.indexOf(classification)));
                compareResults(classification, classificationType, Algorithm.KNN);
            });
        }
    }
}
