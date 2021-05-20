package com.github.amkaras.tweetus.external.twitter.service;

import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.entity.TweetReference;
import com.github.amkaras.tweetus.external.twitter.entity.TweetState;
import com.github.amkaras.tweetus.external.twitter.repository.ReferencedTweetRepository;
import com.github.amkaras.tweetus.external.twitter.repository.TweetRepository;
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
    public List<Tweet> findNotAnalyzedWithOpinionFinder(int maxResults) {
        return tweetRepository.findByAnalyzedWithOpinionFinderOrderByRetweetCountDesc(false, PageRequest.of(0, maxResults));
    }

    @Override
    public List<Tweet> findBelongingToTrainingSet(int maxResults) {
        return tweetRepository.findByBelongsToTrainingSet(true, PageRequest.of(0, maxResults));
    }

    @Override
    public List<Tweet> findAnalyzedNotBelongingToTrainingSet(int maxResults) {
        return tweetRepository.findByBelongsToTrainingSetAndAnalyzedWithOpinionFinder(
                false, true, PageRequest.of(0, maxResults));
    }

    @Override
    public void markAllAsNotBelongingToTrainingSet() {
        tweetRepository.markAllAsNotBelongingToTrainingSet();
    }

    @Override
    public void markAsBelongingToTrainingSet(int count) {
        var ids = tweetRepository.findIdsByAnalyzedWithOpinionFinder(true, PageRequest.of(0, count));
        tweetRepository.markAsBelongingToTrainingSet(ids);
    }

    @Override
    public void markAsAnalyzedWithOpinionFinder(List<String> ids) {
        tweetRepository.markAsAnalyzedWithOpinionFinder(ids);
    }
}
