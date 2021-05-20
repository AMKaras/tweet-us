package com.github.amkaras.tweetus.external.twitter.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "referenced_tweets")
public class TweetReference {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "source_tweet_id")
    private String sourceTweetId;

    @Column(name = "referenced_tweet_id")
    private String referencedTweetId;

    @Column(name = "reference_type")
    @Convert(converter = ReferenceTypeConverter.class)
    private ReferenceType referenceType;

    public UUID getId() {
        return id;
    }

    public String getSourceTweetId() {
        return sourceTweetId;
    }

    public String getReferencedTweetId() {
        return referencedTweetId;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setSourceTweetId(String sourceTweetId) {
        this.sourceTweetId = sourceTweetId;
    }

    public void setReferencedTweetId(String referencedTweetId) {
        this.referencedTweetId = referencedTweetId;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }
}
