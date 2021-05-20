package com.github.amkaras.tweetus.configuration;

import com.github.amkaras.tweetus.common.model.ClassificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeatureToggles {

    private final boolean twitterClientEnabled;
    private final int twitterClientBatchSize;
    private final boolean opinionFinderClientEnabled;
    private final int opinionFinderClientBatchSize;
    private final boolean stanfordNlpClasspathConfigured;
    private final boolean bayesAnalysisExecutorEnabled;
    private final ClassificationType bayesAnalysisClassificationType;
    private final int bayesAnalysisTrainingSetSize;
    private final int bayesAnalysisTestSetSize;
    private final boolean bayesAnalysisLemmatizedModeEnabled;
    private final boolean bayesAnalysisNonLemmatizedModeEnabled;
    private final ClassificationType knnAnalysisClassificationType;
    private final boolean knnAnalysisExecutorEnabled;
    private final int knnAnalysisTrainingSetSize;
    private final int knnAnalysisTestSetSize;
    private final boolean knnAnalysisLemmatizedModeEnabled;
    private final boolean knnAnalysisNonLemmatizedModeEnabled;
    private final List<Integer> knnAnalysisParameterK;


    public FeatureToggles(@Value("${toggle.twitter.client.enabled}") boolean twitterClientEnabled,
                          @Value("${toggle.twitter.client.batch.size}") int twitterClientBatchSize,
                          @Value("${toggle.opinionfinder.client.enabled}") boolean opinionFinderClientEnabled,
                          @Value("${toggle.opinionfinder.client.batch.size}") int opinionFinderClientBatchSize,
                          @Value("${toggle.stanfordnlp.classpath.configured}") boolean stanfordNlpClasspathConfigured,
                          @Value("${toggle.bayes.analysis.executor.enabled}") boolean bayesAnalysisExecutorEnabled,
                          @Value("${toggle.bayes.analysis.classification.type}") String bayesAnalysisClassificationType,
                          @Value("${toggle.bayes.analysis.training.set.size}") int bayesAnalysisTrainingSetSize,
                          @Value("${toggle.bayes.analysis.test.set.size}") int bayesAnalysisTestSetSize,
                          @Value("${toggle.bayes.analysis.mode.enabled.lemmatized}") boolean bayesAnalysisLemmatizedModeEnabled,
                          @Value("${toggle.bayes.analysis.mode.enabled.nonLemmatized}") boolean bayesAnalysisNonLemmatizedModeEnabled,
                          @Value("${toggle.knn.analysis.executor.enabled}") boolean knnAnalysisExecutorEnabled,
                          @Value("${toggle.knn.analysis.classification.type}") String knnAnalysisClassificationType,
                          @Value("${toggle.knn.analysis.training.set.size}") int knnAnalysisTrainingSetSize,
                          @Value("${toggle.knn.analysis.test.set.size}") int knnAnalysisTestSetSize,
                          @Value("${toggle.knn.analysis.mode.enabled.lemmatized}") boolean knnAnalysisLemmatizedModeEnabled,
                          @Value("${toggle.knn.analysis.mode.enabled.nonLemmatized}") boolean knnAnalysisNonLemmatizedModeEnabled,
                          @Value("#{'${toggle.knn.analysis.parameter.k}'.split(',')}") List<Integer> knnAnalysisParameterK) {
        this.twitterClientEnabled = twitterClientEnabled;
        this.twitterClientBatchSize = twitterClientBatchSize;
        this.opinionFinderClientEnabled = opinionFinderClientEnabled;
        this.opinionFinderClientBatchSize = opinionFinderClientBatchSize;
        this.stanfordNlpClasspathConfigured = stanfordNlpClasspathConfigured;
        this.bayesAnalysisExecutorEnabled = bayesAnalysisExecutorEnabled;
        this.bayesAnalysisClassificationType = ClassificationType.valueOf(bayesAnalysisClassificationType);
        this.bayesAnalysisTrainingSetSize = bayesAnalysisTrainingSetSize;
        this.bayesAnalysisTestSetSize = bayesAnalysisTestSetSize;
        this.bayesAnalysisLemmatizedModeEnabled = bayesAnalysisLemmatizedModeEnabled;
        this.bayesAnalysisNonLemmatizedModeEnabled = bayesAnalysisNonLemmatizedModeEnabled;
        this.knnAnalysisExecutorEnabled = knnAnalysisExecutorEnabled;
        this.knnAnalysisClassificationType = ClassificationType.valueOf(knnAnalysisClassificationType);
        this.knnAnalysisTrainingSetSize = knnAnalysisTrainingSetSize;
        this.knnAnalysisTestSetSize = knnAnalysisTestSetSize;
        this.knnAnalysisLemmatizedModeEnabled = knnAnalysisLemmatizedModeEnabled;
        this.knnAnalysisNonLemmatizedModeEnabled = knnAnalysisNonLemmatizedModeEnabled;
        this.knnAnalysisParameterK = knnAnalysisParameterK;
    }

    public boolean isTwitterClientEnabled() {
        return twitterClientEnabled;
    }

    public int getTwitterClientBatchSize() {
        return twitterClientBatchSize;
    }

    public boolean isOpinionFinderClientEnabled() {
        return opinionFinderClientEnabled;
    }

    public int getOpinionFinderClientBatchSize() {
        return opinionFinderClientBatchSize;
    }

    public boolean isStanfordNlpClasspathConfigured() {
        return stanfordNlpClasspathConfigured;
    }

    public boolean isBayesAnalysisExecutorEnabled() {
        return bayesAnalysisExecutorEnabled;
    }

    public ClassificationType getBayesAnalysisClassificationType() {
        return bayesAnalysisClassificationType;
    }

    public int getBayesAnalysisTrainingSetSize() {
        return bayesAnalysisTrainingSetSize;
    }

    public int getBayesAnalysisTestSetSize() {
        return bayesAnalysisTestSetSize;
    }

    public boolean isBayesAnalysisLemmatizedModeEnabled() {
        return bayesAnalysisLemmatizedModeEnabled;
    }

    public boolean isBayesAnalysisNonLemmatizedModeEnabled() {
        return bayesAnalysisNonLemmatizedModeEnabled;
    }

    public ClassificationType getKnnAnalysisClassificationType() {
        return knnAnalysisClassificationType;
    }

    public boolean isKnnAnalysisExecutorEnabled() {
        return knnAnalysisExecutorEnabled;
    }

    public int getKnnAnalysisTrainingSetSize() {
        return knnAnalysisTrainingSetSize;
    }

    public int getKnnAnalysisTestSetSize() {
        return knnAnalysisTestSetSize;
    }

    public boolean isKnnAnalysisLemmatizedModeEnabled() {
        return knnAnalysisLemmatizedModeEnabled;
    }

    public boolean isKnnAnalysisNonLemmatizedModeEnabled() {
        return knnAnalysisNonLemmatizedModeEnabled;
    }

    public List<Integer> getKnnAnalysisParameterK() {
        return knnAnalysisParameterK;
    }
}
