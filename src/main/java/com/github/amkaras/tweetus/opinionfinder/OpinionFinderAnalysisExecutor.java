package com.github.amkaras.tweetus.opinionfinder;

import com.github.amkaras.tweetus.opinionfinder.entity.AnalysisEntity;
import com.github.amkaras.tweetus.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.opinionfinder.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.twitter.entity.Tweet;
import com.github.amkaras.tweetus.twitter.entity.TweetState;
import com.github.amkaras.tweetus.twitter.service.TweetService;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OpinionFinderAnalysisExecutor {

    private static final Logger log = LoggerFactory.getLogger(OpinionFinderAnalysisExecutor.class);

    private final OpinionFinderAnalysisService opinionFinderAnalysisService;
    private final TweetService tweetService;
    private final OpinionFinderClient opinionFinderClient = new OpinionFinderClient();

    private AtomicBoolean shouldProceedWithAnalysis = new AtomicBoolean(true);
    private AtomicReference<TweetState> availableTweetState = new AtomicReference<>(TweetState.FETCHED);

    @Autowired
    public OpinionFinderAnalysisExecutor(OpinionFinderAnalysisService opinionFinderAnalysisService, TweetService tweetService) {
        this.opinionFinderAnalysisService = opinionFinderAnalysisService;
        this.tweetService = tweetService;
    }

    @Scheduled(fixedDelay = 1_000)
    public void analyzeTweets() {
        if (shouldProceedWithAnalysis.get()) {
            var batchSize = 100;
            var successfullyAnalyzedCount = 0;
            var sw = Stopwatch.createStarted();
            var nonAnalyzedTweets = tweetService
                    .findByStateAndIsAnalyzed(availableTweetState.get(), false, batchSize);
            final Set<OpinionFinderAnalysis> analyses = new HashSet<>(batchSize);
            final Set<Tweet> analyzedTweets = new HashSet<>(batchSize);
            for (Tweet tweet : nonAnalyzedTweets) {
                try {
                    var opinionFinderAnalysis = opinionFinderClient.analyze(tweet);
                    opinionFinderAnalysis.setEntity(AnalysisEntity.TWEET);
                    opinionFinderAnalysis.setEntityId(tweet.getId());
                    analyses.add(opinionFinderAnalysis);
                    tweet.setAnalyzedWithOpinionFinder(true);
                    analyzedTweets.add(tweet);
                    successfullyAnalyzedCount += 1;
                } catch (Exception e) {
                    log.error("Error while analyzing tweet {}", tweet.getId(), e);
                }
            }
            opinionFinderAnalysisService.save(analyses);
            tweetService.save(analyzedTweets);
            log.info("Successfully analyzed {} out of {} tweets in {}", successfullyAnalyzedCount, batchSize, sw.stop());
        }
    }

    //    @Scheduled(fixedRate = 1_000 * 60 * 5, initialDelay = 1_000 * 60)
    public void updateAnalysisStatus() {
        if (shouldProceedWithAnalysis.get()) {
            var remaining = tweetService.countByStateAndAnalyzedWithOpinionFinder(availableTweetState.get(), false);
            if (remaining == 0) {
                var otherState = availableTweetState.get() == TweetState.FETCHED ? TweetState.ADDED : TweetState.FETCHED;
                availableTweetState.set(otherState);
                var remainingForOtherState = tweetService.countByStateAndAnalyzedWithOpinionFinder(otherState, false);
                if (remainingForOtherState == 0) {
                    log.info("Setting shouldProceedWithAnalysis flag to false");
                    shouldProceedWithAnalysis.set(false);
                }
            }
        } else {
            var remaining = tweetService.countByStateAndAnalyzedWithOpinionFinder(availableTweetState.get(), false);
            if (remaining > 0) {
                shouldProceedWithAnalysis.set(true);
            } else {
                var otherState = availableTweetState.get() == TweetState.FETCHED ? TweetState.ADDED : TweetState.FETCHED;
                availableTweetState.set(otherState);
                var remainingForOtherState = tweetService.countByStateAndAnalyzedWithOpinionFinder(otherState, false);
                if (remainingForOtherState > 0) {
                    log.info("Setting shouldProceedWithAnalysis flag to true");
                    shouldProceedWithAnalysis.set(true);
                }
            }
        }
    }
}
