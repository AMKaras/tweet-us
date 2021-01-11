package com.github.amkaras.tweetus.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicMetrics {

    private final int retweetCount;
    private final int replyCount;
    private final int likeCount;
    private final int quoteCount;

    @JsonCreator
    public PublicMetrics(@JsonProperty("retweet_count") int retweetCount,
                         @JsonProperty("reply_count") int replyCount,
                         @JsonProperty("like_count") int likeCount,
                         @JsonProperty("quote_count") int quoteCount) {
        this.retweetCount = retweetCount;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
        this.quoteCount = quoteCount;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getQuoteCount() {
        return quoteCount;
    }
}
