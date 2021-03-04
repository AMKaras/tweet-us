package com.github.amkaras.tweetus.entity;


import com.github.amkaras.tweetus.entity.converter.TweetStateConverter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;


@Entity
@Table(name = "tweets")
public class Tweet {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "state")
    @Convert(converter = TweetStateConverter.class)
    private TweetState state;

    @Column(name = "collection")
    private String collection;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "author_username")
    private String authorUsername;

    @Column(name = "content")
    private String content;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "retweet_count")
    private Integer retweetCount;

    @Column(name = "reply_count")
    private Integer replyCount;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "quote_count")
    private Integer quoteCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "source")
    private String source;

    @Column(name = "possibly_sensitive")
    private Boolean possiblySensitive;

    @Column(name = "lang")
    private String language;

    @Column(name = "analyzed_with_opinion_finder")
    private boolean analyzedWithOpinionFinder;

    @Column(name = "belongs_to_training_set")
    private boolean belongsToTrainingSet;

    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Hashtag> hashtags;

    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MentionedUser> mentionedUsers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TweetState getState() {
        return state;
    }

    public void setState(TweetState state) {
        this.state = state;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(Integer retweetCount) {
        this.retweetCount = retweetCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(Integer quoteCount) {
        this.quoteCount = quoteCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getPossiblySensitive() {
        return possiblySensitive;
    }

    public void setPossiblySensitive(Boolean possiblySensitive) {
        this.possiblySensitive = possiblySensitive;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isAnalyzedWithOpinionFinder() {
        return analyzedWithOpinionFinder;
    }

    public void setAnalyzedWithOpinionFinder(boolean analyzedWithOpinionFinder) {
        this.analyzedWithOpinionFinder = analyzedWithOpinionFinder;
    }

    public boolean isBelongsToTrainingSet() {
        return belongsToTrainingSet;
    }

    public void setBelongsToTrainingSet(boolean belongsToTrainingSet) {
        this.belongsToTrainingSet = belongsToTrainingSet;
    }

    public Set<Hashtag> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<Hashtag> hashtags) {
        this.hashtags = hashtags;
    }

    public Set<MentionedUser> getMentionedUsers() {
        return mentionedUsers;
    }

    public void setMentionedUsers(Set<MentionedUser> mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
    }
}
