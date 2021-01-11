package com.github.amkaras.tweetus.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "hashtags")
public class Hashtag {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "tweet_id", referencedColumnName = "id", updatable = false)
    private Tweet tweet;

    @Column(name = "tag")
    private String tag;

    public UUID getId() {
        return id;
    }

    public Tweet getTweet() {
        return tweet;
    }

    public String getTag() {
        return tag;
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hashtag hashtag = (Hashtag) o;
        return getId().equals(hashtag.getId()) &&
                getTweet().equals(hashtag.getTweet()) &&
                getTag().equals(hashtag.getTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTweet(), getTag());
    }
}
