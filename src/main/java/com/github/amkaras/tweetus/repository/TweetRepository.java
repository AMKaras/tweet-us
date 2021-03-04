package com.github.amkaras.tweetus.repository;

import com.github.amkaras.tweetus.entity.Tweet;
import com.github.amkaras.tweetus.entity.TweetState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {

    int countByStateAndAnalyzedWithOpinionFinderAndLanguage(
            TweetState state, boolean analyzedWithOpinionFinder, String language);

    List<Tweet> findByState(TweetState state, Pageable pageable);

    List<Tweet> findByStateAndAnalyzedWithOpinionFinderAndLanguageOrderByRetweetCount(
            TweetState tweetState, boolean analyzedWithOpinionFinder, String language, Pageable pageable);

    List<Tweet> findByBelongsToTrainingSetAndAnalyzedWithOpinionFinderAndLanguageOrderByRetweetCount(
            boolean belongsToTrainingSet, boolean analyzedWithOpinionFinder, String language, Pageable pageable);
}
