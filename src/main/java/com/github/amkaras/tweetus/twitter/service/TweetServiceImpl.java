package com.github.amkaras.tweetus.twitter.service;

import com.github.amkaras.tweetus.twitter.entity.Tweet;
import com.github.amkaras.tweetus.twitter.entity.TweetReference;
import com.github.amkaras.tweetus.twitter.entity.TweetState;
import com.github.amkaras.tweetus.twitter.repository.ReferencedTweetRepository;
import com.github.amkaras.tweetus.twitter.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Transactional
@Service
public class TweetServiceImpl implements TweetService {

    private static final String LANG_EN = "en";

    private final TweetRepository tweetRepository;
    private final ReferencedTweetRepository referencedTweetRepository;

    @Autowired
    public TweetServiceImpl(TweetRepository tweetRepository, ReferencedTweetRepository referencedTweetRepository) {
        this.tweetRepository = tweetRepository;
        this.referencedTweetRepository = referencedTweetRepository;
    }

    @Override
    public void save(Tweet tweet) {
        save(tweet, null);
    }

    @Override
    public void save(Iterable<Tweet> tweets) {
        tweetRepository.saveAll(tweets);
    }

    @Override
    public void save(Tweet tweet, Set<Tweet> referencedTweets) {
        tweetRepository.save(tweet);
        if (referencedTweets != null) {
            var tweetsToBeAdded = referencedTweets.stream()
                    .filter(rt -> !tweetRepository.existsById(rt.getId()))
                    .collect(toSet());
            tweetRepository.saveAll(tweetsToBeAdded);
        }
    }

    @Override
    public void saveAll(Iterable<TweetReference> tweetReferences) {
        referencedTweetRepository.saveAll(tweetReferences);
    }

    @Override
    public List<Tweet> findByState(TweetState state, int maxResults) {
        return tweetRepository.findByState(state, PageRequest.of(0, maxResults));
    }

    @Override
    public List<Tweet> findByStateAndIsAnalyzed(TweetState state, boolean isAnalyzed, int maxResults) {
        return tweetRepository.findByStateAndAnalyzedWithOpinionFinderAndLanguageOrderByRetweetCount(
                state, isAnalyzed, LANG_EN, PageRequest.of(0, maxResults));
    }

    @Override
    public List<Tweet> findBelongingToTrainingSet(int maxResults) {
        return tweetRepository.findByBelongsToTrainingSetAndAnalyzedWithOpinionFinderAndLanguageOrderByRetweetCount(
                true, true, LANG_EN, PageRequest.of(0, maxResults));
    }

    @Override
    public List<Tweet> findAnalyzedNotBelongingToTrainingSet(int maxResults) {
        return tweetRepository.findByBelongsToTrainingSetAndAnalyzedWithOpinionFinderAndLanguageOrderByRetweetCount(
                false, true, LANG_EN, PageRequest.of(0, maxResults));
    }

    @Override
    public int countByStateAndAnalyzedWithOpinionFinder(TweetState state, boolean analyzedWithOpinionFinder) {
        return tweetRepository.countByStateAndAnalyzedWithOpinionFinderAndLanguage(
                state, analyzedWithOpinionFinder, LANG_EN);
    }
}
