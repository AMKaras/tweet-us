package com.github.amkaras.tweetus.common.analysis;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class KNNTweetsAnalysisExecutor extends AnalysisExecutor {

    private final DocumentsBuilder documentsBuilder;
    private Set<Document> documents;
    private Set<Document> nonLemmatizedDocuments;
    private Map<Tweet, Optional<ClassificationCategory>> knnClassifications;
    private Map<Tweet, Optional<ClassificationCategory>> nonLemmatizedKNNClassifications;

    @Autowired
    public KNNTweetsAnalysisExecutor(TweetService tweetService,
                                     OpinionFinderAnalysisService opinionFinderAnalysisService,
                                     FeatureToggles featureToggles) {
        super(tweetService, opinionFinderAnalysisService, featureToggles);
        final StanfordLemmatizerClient lemmatizerClient = featureToggles.isStanfordNlpClasspathConfigured() ?
                StanfordLemmatizerClient.createConfigured() : StanfordLemmatizerClient.createNotConfigured();
        this.algorithm = new KNNClassificationAlgorithm(lemmatizerClient);
        this.documentsBuilder = new DocumentsBuilder(lemmatizerClient);
    }

    @PostConstruct
    @Override
    public void analyze() {

        if (!featureToggles.isKnnAnalysisExecutorEnabled() || comparativeAnalysisEnabled()) {
            return;
        }

        ClassificationType classificationType = featureToggles.getKnnAnalysisClassificationType();
        List<Integer> k = featureToggles.getKnnAnalysisParameterK();
        boolean lemmatizedModeEnabled = featureToggles.isKnnAnalysisLemmatizedModeEnabled();
        boolean nonLemmatizedModeEnabled = featureToggles.isKnnAnalysisNonLemmatizedModeEnabled();

        prepareTrainingSet(featureToggles.getKnnAnalysisTrainingSetSize());

        log.info("Processing analyses to documents");

        if (lemmatizedModeEnabled) {
            documents = documentsBuilder.build(trainingSet, trainingSetAnalyses, classificationType, true);
            log.info("Processed. Documents are {}", documents);
        }
        if (nonLemmatizedModeEnabled) {
            nonLemmatizedDocuments = documentsBuilder.build(trainingSet, trainingSetAnalyses, classificationType, false);
            log.info("Processed. Non lemmatized documents are {}", nonLemmatizedDocuments);
        }

        prepareTestSet(featureToggles.getKnnAnalysisTestSetSize());


        k.forEach(currentK -> {
            if (lemmatizedModeEnabled) {
                log.info("Classifying with lemmatization enabled:");
                knnClassifications = algorithm.classify(testSet, documents, true, currentK);
            }
            if (nonLemmatizedModeEnabled) {
                log.info("Classifying with lemmatization disabled:");
                nonLemmatizedKNNClassifications = algorithm.classify(testSet, nonLemmatizedDocuments, false, currentK);
                log.info("Classified {} tweets", nonLemmatizedKNNClassifications.size());
            }

            if (lemmatizedModeEnabled) {
                log.info("Lemmatized analyses results:");
                compareResults(knnClassifications, classificationType, Algorithm.KNN);
            }
            if (nonLemmatizedModeEnabled) {
                log.info("Non lemmatized analyses results:");
                compareResults(nonLemmatizedKNNClassifications, classificationType, Algorithm.KNN);
            }
        });
    }
}
