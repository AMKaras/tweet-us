package com.github.amkaras.tweetus.external.twitter.service;

import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.entity.TweetReference;
import com.github.amkaras.tweetus.external.twitter.entity.TweetState;

import java.util.List;
import java.util.Set;

public interface TweetService {

    void save(Tweet tweet);

    void save(Tweet tweet, Set<Tweet> referencedTweets);

    void saveAll(Iterable<TweetReference> tweetReferences);

    List<Tweet> findByState(TweetState state, int maxResults);

    List<Tweet> findNotAnalyzedWithOpinionFinder(int maxResults);

    List<Tweet> findBelongingToTrainingSet(int maxResults);

    List<Tweet> findAnalyzedNotBelongingToTrainingSet(int maxResults);

    void markAllAsNotBelongingToTrainingSet();

    void markAsBelongingToTrainingSet(int count);

    void markAsAnalyzedWithOpinionFinder(List<String> ids);
}
