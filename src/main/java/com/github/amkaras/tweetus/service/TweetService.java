package com.github.amkaras.tweetus.service;

import com.github.amkaras.tweetus.entity.Tweet;
import com.github.amkaras.tweetus.entity.TweetReference;
import com.github.amkaras.tweetus.entity.TweetState;

import java.util.List;
import java.util.Set;

public interface TweetService {

    void save(Tweet tweet);

    void save(Tweet tweet, Set<Tweet> referencedTweets);

    void saveAll(Iterable<TweetReference> tweetReferences);

    List<Tweet> findTweetsByIds(Iterable<String> tweetIds);

    List<Tweet> findTweetsByState(TweetState state, int maxResults);
}
