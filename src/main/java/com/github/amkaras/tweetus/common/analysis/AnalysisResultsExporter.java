package com.github.amkaras.tweetus.common.analysis;

import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.logic.WeightedClassificationCategorySelector;
import com.github.amkaras.tweetus.common.model.BinaryClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.common.model.ClassificationType;
import com.github.amkaras.tweetus.common.model.DifferentialClassificationCategory;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.amkaras.tweetus.common.model.ClassificationType.DIFFERENTIAL;
import static java.lang.String.format;
import static java.math.RoundingMode.HALF_UP;

public class AnalysisResultsExporter {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
    private static final String RESULTS_DIR = "results";
    private static final String CSV = ".csv";
    private static final String NEW_LINE = "\n";
    private static final String N_A = "N/A";

    private final WeightedClassificationCategorySelector categorySelector = new WeightedClassificationCategorySelector();

    public void export(List<Tweet> trainingSet, List<OpinionFinderAnalysis> trainingSetAnalyses,
                       List<Tweet> testSet, List<OpinionFinderAnalysis> testSetAnalyses,
                       Map<ClassificationCategory, Map<String, Long>> dictionary,
                       Map<ClassificationCategory, Map<String, Long>> nonLemmatizedDictionary,
                       Set<Document> documents, Set<Document> nonLemmatizedDocuments,
                       Map<Tweet, Optional<ClassificationCategory>> bayesClassifications,
                       Map<Tweet, Optional<ClassificationCategory>> nonLemmatizedBayesClassifications,
                       List<Map<Tweet, Optional<ClassificationCategory>>> knnClassifications,
                       List<Map<Tweet, Optional<ClassificationCategory>>> nonLemmatizedKNNClassifications,
                       List<Integer> valuesOfParameterK, ClassificationType classificationType,
                       Map<String, AnalysisResults> resultsPerClassification)
            throws IOException {
        String now = SDF.format(new Date());
        String dir = RESULTS_DIR + now;
        new File(dir).mkdir();
        Map<String, List<String>> results = new HashMap<>();
        results.put("training_set", trainingSet(trainingSet, trainingSetAnalyses, classificationType));
        results.put("test_set_with_results", testSetWithResults(
                testSet, testSetAnalyses, bayesClassifications, nonLemmatizedBayesClassifications,
                knnClassifications, nonLemmatizedKNNClassifications, valuesOfParameterK, classificationType));
        results.put("knn_lemmatized_documents", documents(documents));
        results.put("knn_non_lemmatized_documents", documents(nonLemmatizedDocuments));
        results.put("bayes_lemmatized_dictionary", dictionary(dictionary));
        results.put("bayes_non_lemmatized_dictionary", dictionary(nonLemmatizedDictionary));
        results.put("summary", summary(resultsPerClassification));
        for (Map.Entry<String, List<String>> filenameWithContent : results.entrySet()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "/" + filenameWithContent.getKey() + CSV));
            for (String line : filenameWithContent.getValue()) {
                writer.write(line);
                writer.write(NEW_LINE);
            }
            writer.close();
        }
    }

    private List<String> summary(Map<String, AnalysisResults> resultsPerAlgorithm) {
        List<String> rows = new ArrayList<>(resultsPerAlgorithm.size());
        resultsPerAlgorithm.forEach((algorithm, summary) -> {
            String row = format("%s,%s,%s,%s",
                    algorithm,
                    BigDecimal.valueOf(summary.getConsistentCount()).setScale(0, HALF_UP).toString(),
                    BigDecimal.valueOf(summary.getTotalCount()).setScale(0, HALF_UP).toString(),
                    BigDecimal.valueOf(summary.getAccuracy()).setScale(2, HALF_UP).toString()) + "%";
            rows.add(row);
        });
        Collections.sort(rows);
        List<String> csv = new ArrayList<>(rows.size() + 1);
        String header = "Algorithm,Consistent count,Total count,Accuracy";
        csv.add(header);
        csv.addAll(rows);
        return csv;
    }

    private List<String> trainingSet(List<Tweet> trainingSet, List<OpinionFinderAnalysis> trainingSetAnalyses, ClassificationType classificationType) {
        List<String> csv = new ArrayList<>(trainingSet.size() + 1);
        String header = "Tweet ID,Tweet content,OpinionFinder classification";
        csv.add(header);
        trainingSet.forEach(tweet -> {
            String opinionFinderClassification = getOpinionFinderClassification(tweet, trainingSetAnalyses, classificationType);
            if (!N_A.equals(opinionFinderClassification)) {
                csv.add(format("%s,%s,%s", tweet.getId(), csvEscaped(tweet.getContent()), opinionFinderClassification));
            }
        });
        return csv;
    }

    private List<String> testSetWithResults(
            List<Tweet> testSet, List<OpinionFinderAnalysis> testSetAnalyses,
            Map<Tweet, Optional<ClassificationCategory>> bayesClassifications,
            Map<Tweet, Optional<ClassificationCategory>> nonLemmatizedBayesClassifications,
            List<Map<Tweet, Optional<ClassificationCategory>>> knnClassifications,
            List<Map<Tweet, Optional<ClassificationCategory>>> nonLemmatizedKNNClassifications,
            List<Integer> valuesOfParameterK, ClassificationType classificationType) {
        List<String> csv = new ArrayList<>(testSet.size() + 1);
        String header = "Tweet ID,Tweet content,OpinionFinder classification," +
                "Bayes lemmatized classification,Bayes non lemmatized classification";
        for (int i = 0; i < valuesOfParameterK.size(); ++i) {
            header += ",KNN lemmatized classification K=" + valuesOfParameterK.get(i);
            header += ",KNN non lemmatized classification K=" + valuesOfParameterK.get(i);
        }
        csv.add(header);
        testSet.forEach(tweet -> {
            String opinionFinderClassification = getOpinionFinderClassification(tweet, testSetAnalyses, classificationType);
            if (!N_A.equals(opinionFinderClassification)) {
                String bayesLemmatized = getAlgorithmClassification(tweet, bayesClassifications);
                String bayesNonLemmatized = getAlgorithmClassification(tweet, nonLemmatizedBayesClassifications);
                String row = format("%s,%s,%s,%s,%s", tweet.getId(), csvEscaped(tweet.getContent()),
                        opinionFinderClassification, bayesLemmatized, bayesNonLemmatized);
                for (int j = 0; j < valuesOfParameterK.size(); ++j) {
                    row += format(",%s", getAlgorithmClassification(tweet, knnClassifications.get(j)));
                    row += format(",%s", getAlgorithmClassification(tweet, nonLemmatizedKNNClassifications.get(j)));
                }
                csv.add(row);
            }
        });
        return csv;
    }

    private String csvEscaped(String nonCsvEscaped) {
        return format("\"%s\"", nonCsvEscaped.replace("\"", "\"\"").replace("\n", " "));
    }

    private String getOpinionFinderClassification(Tweet tweet, List<OpinionFinderAnalysis> analyses, ClassificationType classificationType) {
        OpinionFinderAnalysis opinionFinderAnalysis = analyses.stream()
                .filter(analysis -> analysis.getEntityId().equals(tweet.getId()))
                .findFirst()
                .get();
        Optional<DifferentialClassificationCategory> maybeOpinionFinderDifferentialClassification =
                categorySelector.select(opinionFinderAnalysis.getSubjectiveClues(), opinionFinderAnalysis.getPolarityClassifiers());
        return maybeOpinionFinderDifferentialClassification.isPresent() ?
                classificationType == DIFFERENTIAL ?
                        maybeOpinionFinderDifferentialClassification.get().toString() :
                        BinaryClassificationCategory.map(maybeOpinionFinderDifferentialClassification.get()).toString()
                : N_A;
    }

    private String getAlgorithmClassification(Tweet tweet, Map<Tweet, Optional<ClassificationCategory>> classifications) {
        Optional<Map.Entry<Tweet, Optional<ClassificationCategory>>> maybeEntry = classifications.entrySet().stream()
                .filter(entry -> tweet.getId().equals(entry.getKey().getId()))
                .findFirst();
        Optional<ClassificationCategory> maybeClassification = maybeEntry.isPresent() ?
                maybeEntry.get().getValue() : Optional.empty();
        return maybeClassification.isPresent() ? maybeClassification.get().toString() : N_A;
    }

    private List<String> dictionary(Map<ClassificationCategory, Map<String, Long>> dictionary) {
        List<String> rows = new ArrayList<>();
        dictionary.forEach((category, tokensWithCount) -> tokensWithCount.forEach((token, count) -> {
            rows.add(format("%s,%s,%s", category, token, count));
        }));
        Collections.sort(rows);
        List<String> csv = new ArrayList<>(rows.size() + 1);
        String header = "Category,Token,Count";
        csv.add(header);
        csv.addAll(rows);
        return csv;
    }

    private List<String> documents(Set<Document> documents) {
        List<String> rows = new ArrayList<>(documents.size());
        documents.forEach(document ->
                rows.add(format("%s,%s,%s", document.getId(), document.getContent(), document.getCategory())));
        Collections.sort(rows);
        List<String> csv = new ArrayList<>(rows.size() + 1);
        String header = "Document ID,Content,Category";
        csv.add(header);
        csv.addAll(rows);
        return csv;
    }
}
