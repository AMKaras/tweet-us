package com.github.amkaras.tweetus.twitter;

import com.github.amkaras.tweetus.twitter.entity.MentionedUser;
import com.github.amkaras.tweetus.twitter.entity.ReferenceType;
import com.github.amkaras.tweetus.twitter.entity.Tweet;
import com.github.amkaras.tweetus.twitter.entity.TweetReference;
import com.github.amkaras.tweetus.twitter.entity.TweetState;
import com.github.amkaras.tweetus.twitter.model.Data;
import com.github.amkaras.tweetus.twitter.model.Hashtag;
import com.github.amkaras.tweetus.twitter.model.Mention;
import com.github.amkaras.tweetus.twitter.model.ReferencedTweet;
import com.github.amkaras.tweetus.twitter.model.TweetPayload;
import com.github.amkaras.tweetus.twitter.model.User;
import com.github.amkaras.tweetus.twitter.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Component
public class TweetPayloadProcessor {

    private static final Logger log = LoggerFactory.getLogger(TweetPayloadProcessor.class);

    private final TweetService tweetService;

    @Autowired
    public TweetPayloadProcessor(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    public boolean accept(Tweet dbTweet, TweetPayload payload) {
        return setData(dbTweet, payload);
    }

    private boolean setData(Tweet dbTweet, TweetPayload payload) {
        var data = payload.getData();
        if (data != null) {
            setBasicTweetData(data, dbTweet);
            Set<Tweet> dbAddedTweets = null;
            var tweetReferences = buildTweetReferences(dbTweet, data);
            var includes = payload.getIncludes();
            if (includes != null) {
                var includedUsers = includes.getUsers();
                var includedTweets = includes.getTweets();
                if (includedUsers != null) {
                    setUserDetailsFromIncludes(dbTweet, includedUsers);
                }
                if (includedTweets != null) {
                    dbAddedTweets = buildIncludedTweets(includedTweets, includedUsers);
                }
            }
            dbTweet.setState(TweetState.FETCHED);
            tweetService.save(dbTweet, dbAddedTweets);
            tweetService.saveAll(tweetReferences);
            log.debug("Tweet {} fetched successfully", dbTweet.getId());
            return true;
        } else {
            dbTweet.setState(TweetState.UNAVAILABLE);
            tweetService.save(dbTweet);
            log.debug("Tweet {} unavailable", dbTweet.getId());
            return false;
        }
    }

    private void setCreatedAt(Tweet dbTweet, Data payloadData) {
        var createdAt = payloadData.getCreatedAt() != null ? Timestamp.valueOf(payloadData.getCreatedAt()) : null;
        dbTweet.setCreatedAt(createdAt);
    }

    private void setPublicMetrics(Tweet dbTweet, Data payloadData) {
        var publicMetrics = payloadData.getPublicMetrics();
        if (publicMetrics == null) {
            return;
        }
        dbTweet.setRetweetCount(publicMetrics.getRetweetCount());
        dbTweet.setReplyCount(publicMetrics.getReplyCount());
        dbTweet.setLikeCount(publicMetrics.getLikeCount());
        dbTweet.setQuoteCount(publicMetrics.getQuoteCount());
    }

    private void setEntities(Tweet dbTweet, Data payloadData) {
        var entities = payloadData.getEntities();
        if (entities != null) {
            var apiHashtags = entities.getHashtags();
            var apiMentions = entities.getMentions();
            if (apiHashtags != null) {
                var dbHashtags = apiHashtags.stream()
                        .map(apiToDbHashtag(dbTweet))
                        .collect(toSet());
                dbTweet.setHashtags(dbHashtags);
            }
            if (apiMentions != null) {
                var dbMentions = apiMentions.stream()
                        .map(toDbMentionedUser(dbTweet))
                        .collect(toSet());
                dbTweet.setMentionedUsers(dbMentions);
            }
        }
    }

    private void setUserDetailsFromIncludes(Tweet dbTweet, List<User> includedUsers) {
        if (includedUsers != null) {
            if (dbTweet.getMentionedUsers() != null) {
                for (MentionedUser dbUser : dbTweet.getMentionedUsers()) {
                    includedUsers.stream()
                            .filter(apiUser -> apiUser.getUsername().equals(dbUser.getUsername()))
                            .findFirst()
                            .ifPresent(apiUser -> dbUser.setUserId(apiUser.getId()));
                }
            }
            includedUsers.stream()
                    .filter(apiUser -> apiUser.getId().equals(dbTweet.getAuthorId()))
                    .findFirst().
                    ifPresent(apiUser -> dbTweet.setAuthorUsername(apiUser.getUsername()));
        }
    }

    private Set<TweetReference> buildTweetReferences(Tweet dbTweet, Data payloadData) {
        var apiReferences = payloadData.getReferencedTweets();
        if (apiReferences != null) {
            return apiReferences.stream()
                    .map(toTweetReference(dbTweet))
                    .collect(toSet());
        }
        return emptySet();
    }

    private Set<Tweet> buildIncludedTweets(List<Data> includedTweets, List<User> includedUsers) {
        final Set<Tweet> dbTweets = new HashSet<>();
        for (Data apiTweet : includedTweets) {
            var dbTweet = new Tweet();
            dbTweet.setState(TweetState.ADDED);
            dbTweet.setId(apiTweet.getId());
            setBasicTweetData(apiTweet, dbTweet);
            setUserDetailsFromIncludes(dbTweet, includedUsers);
            dbTweets.add(dbTweet);
        }
        return dbTweets;
    }

    private void setBasicTweetData(Data apiTweet, Tweet dbTweet) {
        dbTweet.setConversationId(apiTweet.getConversationId());
        dbTweet.setContent(apiTweet.getText());
        dbTweet.setAuthorId(apiTweet.getAuthorId());
        setCreatedAt(dbTweet, apiTweet);
        setPublicMetrics(dbTweet, apiTweet);
        setEntities(dbTweet, apiTweet);
        dbTweet.setPossiblySensitive(apiTweet.isPossiblySensitive());
        dbTweet.setSource(apiTweet.getSource());
        dbTweet.setLanguage(apiTweet.getLang());
    }

    private Function<Hashtag, com.github.amkaras.tweetus.twitter.entity.Hashtag> apiToDbHashtag(Tweet tweet) {
        return apiHashtag -> {
            var dbHashtag = new com.github.amkaras.tweetus.twitter.entity.Hashtag();
            dbHashtag.setTag(apiHashtag.getTag());
            dbHashtag.setTweet(tweet);
            return dbHashtag;
        };
    }

    private Function<Mention, MentionedUser> toDbMentionedUser(Tweet tweet) {
        return apiMention -> {
            var dbMentionedUser = new MentionedUser();
            dbMentionedUser.setUsername(apiMention.getUsername());
            dbMentionedUser.setTweet(tweet);
            return dbMentionedUser;
        };
    }

    private Function<ReferencedTweet, TweetReference> toTweetReference(Tweet tweet) {
        return apiReference -> {
            var dbReference = new TweetReference();
            dbReference.setSourceTweetId(tweet.getId());
            dbReference.setReferencedTweetId(apiReference.getId());
            dbReference.setReferenceType(ReferenceType.valueOf(apiReference.getType().toUpperCase()));
            return dbReference;
        };
    }
}
