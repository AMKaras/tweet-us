package com.github.amkaras.tweetus.api.client;

import com.github.amkaras.tweetus.api.model.TweetPayload;
import com.github.amkaras.tweetus.api.service.TweetPayloadProcessor;
import com.github.amkaras.tweetus.entity.Tweet;
import com.github.amkaras.tweetus.entity.TweetState;
import com.github.amkaras.tweetus.service.TweetService;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class ScheduledTwitterApiClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTwitterApiClient.class);

    private static final String ID = ":ID";
    private static final String BEARER = "Bearer ";
    private final String twitterApiUrl;
    private final List<String> twitterApiTokens;
    private int currentTokenIndex = 0;

    private final TweetService tweetService;
    private final TweetPayloadProcessor tweetPayloadProcessor;
    private final RestTemplate restTemplate;

    @Autowired
    public ScheduledTwitterApiClient(TweetService tweetService,
                                     TweetPayloadProcessor tweetPayloadProcessor,
                                     @Value("${twitter.api.url}") String apiUrl,
                                     @Value("#{'${twitter.api.tokens}'.split(',')}") List<String> apiTokens) {
        this.tweetService = tweetService;
        this.tweetPayloadProcessor = tweetPayloadProcessor;
        this.twitterApiUrl = apiUrl;
        this.twitterApiTokens = apiTokens;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Scheduled(fixedDelayString = "${twitter.api.callInterval}")
    public void fetchTweets() {
        var batchSize = 100;
        var pendingTweets = tweetService.findTweetsByState(TweetState.PENDING, batchSize);
        var token = getTokenAndUpdateCurrentIndex();
        var httpEntity = entityWithAuthHeader(token);
        int successfullyFetched = 0;
        var sw = Stopwatch.createStarted();
        for (Tweet tweet : pendingTweets) {
            var requestUrl = twitterApiUrl.replace(ID, tweet.getId());
            ResponseEntity<TweetPayload> payload = restTemplate
                    .exchange(requestUrl, HttpMethod.GET, httpEntity, TweetPayload.class, emptyMap());
            if (payload.getStatusCode().is2xxSuccessful()) {
                var fetched = tweetPayloadProcessor.accept(tweet, payload.getBody());
                if (fetched) {
                    successfullyFetched += 1;
                }
            } else {
                log.error("Error while using Twitter API. Status code is: {}. Reason is: {}",
                        payload.getStatusCode().value(), payload.getStatusCode().getReasonPhrase());
            }
        }
        log.info("Successfully fetched {} out of {} tweets in {}", successfullyFetched, batchSize, sw.stop());
    }

    private String getTokenAndUpdateCurrentIndex() {
        var currentToken = twitterApiTokens.get(currentTokenIndex);
        currentTokenIndex = currentTokenIndex + 1 < twitterApiTokens.size() ? currentTokenIndex + 1 : 0;
        return currentToken;
    }

    private HttpEntity entityWithAuthHeader(String token) {
        var headers = new HttpHeaders();
        headers.set(AUTHORIZATION, BEARER + token);
        return new HttpEntity(headers);
    }
}
