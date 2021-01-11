package com.github.amkaras.tweetus.service;

import com.github.amkaras.tweetus.entity.Tweet;
import com.github.amkaras.tweetus.entity.TweetReference;
import com.github.amkaras.tweetus.entity.TweetState;
import com.github.amkaras.tweetus.repository.ReferencedTweetRepository;
import com.github.amkaras.tweetus.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public List<Tweet> findTweetsByIds(Iterable<String> tweetIds) {
        return tweetRepository.findAllById(tweetIds);
    }

    @Override
    public List<Tweet> findTweetsByState(TweetState state, int maxResults) {
        return tweetRepository.findByState(state, PageRequest.of(0, maxResults));
    }
}
