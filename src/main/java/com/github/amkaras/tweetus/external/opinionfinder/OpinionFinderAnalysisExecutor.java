package com.github.amkaras.tweetus.external.opinionfinder;

import com.github.amkaras.tweetus.configuration.FeatureToggles;
import com.github.amkaras.tweetus.external.opinionfinder.entity.AnalysisEntity;
import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.opinionfinder.service.OpinionFinderAnalysisService;
import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.service.TweetService;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OpinionFinderAnalysisExecutor {

    private static final Logger log = LoggerFactory.getLogger(OpinionFinderAnalysisExecutor.class);

    private final OpinionFinderAnalysisService opinionFinderAnalysisService;
    private final TweetService tweetService;
    private final FeatureToggles featureToggles;
    private final OpinionFinderClient opinionFinderClient = new OpinionFinderClient();

    @Autowired
    public OpinionFinderAnalysisExecutor(OpinionFinderAnalysisService opinionFinderAnalysisService,
                                         TweetService tweetService, FeatureToggles featureToggles) {
        this.opinionFinderAnalysisService = opinionFinderAnalysisService;
        this.tweetService = tweetService;
        this.featureToggles = featureToggles;
    }

    @Scheduled(fixedDelay = 1_000)
    public void analyzeTweets() {
        if (!featureToggles.isOpinionFinderClientEnabled()) {
            return;
        }
        var batchSize = featureToggles.getOpinionFinderClientBatchSize();
        var successfullyAnalyzedCount = 0;
        var totalSw = Stopwatch.createStarted();
        var partialSw = Stopwatch.createStarted();
        log.info("Starting OpinionFinder analysis");
        var nonAnalyzedTweets = tweetService.findNotAnalyzedWithOpinionFinder(batchSize);
        log.info("Fetched tweets for analysis in {}", partialSw.stop());
        partialSw.reset().start();
        final Set<OpinionFinderAnalysis> analyses = new HashSet<>(batchSize);
        final List<String> analyzedTweetIds = new ArrayList<>(batchSize);
        for (Tweet tweet : nonAnalyzedTweets) {
            try {
                var opinionFinderAnalysis = opinionFinderClient.analyze(tweet);
                opinionFinderAnalysis.setEntity(AnalysisEntity.TWEET);
                opinionFinderAnalysis.setEntityId(tweet.getId());
                analyses.add(opinionFinderAnalysis);
                analyzedTweetIds.add(tweet.getId());
                successfullyAnalyzedCount += 1;
            } catch (Exception e) {
                log.error("Error while analyzing tweet {}", tweet.getId(), e);
            }
        }
        log.info("OpinionFinder analysis execution took {}. Saving results to DB...", partialSw.stop());
        partialSw.reset().start();
        opinionFinderAnalysisService.save(analyses);
        log.info("Saving analyses took {}", partialSw.stop());
        partialSw.reset().start();
        tweetService.markAsAnalyzedWithOpinionFinder(analyzedTweetIds);
        log.info("Marking tweets as analyzed took {}", partialSw.stop());
        log.info("Successfully analyzed {} out of {} tweets in {}", successfullyAnalyzedCount, batchSize, totalSw.stop());

    }
}
