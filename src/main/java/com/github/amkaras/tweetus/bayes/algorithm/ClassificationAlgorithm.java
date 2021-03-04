package com.github.amkaras.tweetus.bayes.algorithm;

import com.github.amkaras.tweetus.bayes.category.ClassificationCategory;
import com.github.amkaras.tweetus.entity.Tweet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClassificationAlgorithm {

    Map<Tweet, Optional<ClassificationCategory>> classify(
            List<Tweet> tweets, Map<ClassificationCategory, Map<String, Long>> dictionary);
}
