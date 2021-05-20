package com.github.amkaras.tweetus.common.algorithm;

import com.github.amkaras.tweetus.common.algorithm.knn.model.Document;
import com.github.amkaras.tweetus.common.model.ClassificationCategory;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ClassificationAlgorithm {

    Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary, boolean lemmatizationEnabled);

    Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Set<Document> classifiedDocuments, boolean lemmatizationEnabled, int k);
}
