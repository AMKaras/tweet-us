package com.github.amkaras.tweetus.external.twitter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {

    private final String id;
    private final String conversationId;
    private final String text;
    private final String authorId;
    private final LocalDateTime createdAt;
    private final PublicMetrics publicMetrics;
    private final Entities entities;
    private final List<ReferencedTweet> referencedTweets;
    private final boolean possiblySensitive;
    private final String source;
    private final String lang;


    @JsonCreator
    public Data(@JsonProperty("id") String id,
                @JsonProperty("conversation_id") String conversationId,
                @JsonProperty("text") String text,
                @JsonProperty("author_id") String authorId,
                @JsonProperty("created_at") LocalDateTime createdAt,
                @JsonProperty("public_metrics") PublicMetrics publicMetrics,
                @JsonProperty("entities") Entities entities,
                @JsonProperty("referenced_tweets") List<ReferencedTweet> referencedTweets,
                @JsonProperty("possibly_sensitive") boolean possiblySensitive,
                @JsonProperty("source") String source,
                @JsonProperty("lang") String lang) {
        this.id = id;
        this.conversationId = conversationId;
        this.text = text;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.publicMetrics = publicMetrics;
        this.entities = entities;
        this.referencedTweets = referencedTweets;
        this.possiblySensitive = possiblySensitive;
        this.source = source;
        this.lang = lang;
    }

    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PublicMetrics getPublicMetrics() {
        return publicMetrics;
    }

    public Entities getEntities() {
        return entities;
    }

    public List<ReferencedTweet> getReferencedTweets() {
        return referencedTweets;
    }

    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    public String getSource() {
        return source;
    }

    public String getLang() {
        return lang;
    }
}
