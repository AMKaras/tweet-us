package com.github.amkaras.tweetus.twitter.service;

import com.github.amkaras.tweetus.twitter.entity.Tweet;
import com.github.amkaras.tweetus.twitter.entity.TweetReference;
import com.github.amkaras.tweetus.twitter.entity.TweetState;

import java.util.List;
import java.util.Set;

public interface TweetService {

    void save(Tweet tweet);

    void save(Iterable<Tweet> tweets);

    void save(Tweet tweet, Set<Tweet> referencedTweets);

    void saveAll(Iterable<TweetReference> tweetReferences);

    List<Tweet> findByState(TweetState state, int maxResults);

    List<Tweet> findByStateAndIsAnalyzed(TweetState state, boolean isAnalyzed, int maxResults);

    List<Tweet> findBelongingToTrainingSet(int maxResults);

    List<Tweet> findAnalyzedNotBelongingToTrainingSet(int maxResults);

    int countByStateAndAnalyzedWithOpinionFinder(TweetState state, boolean analyzedWithOpinionFinder);
}
