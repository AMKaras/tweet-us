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
@Table(name = "mentioned_users")
public class MentionedUser {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "tweet_id", referencedColumnName = "id", updatable = false)
    private Tweet tweet;

    @Column(name = "userId")
    private String userId;

    @Column(name = "username")
    private String username;

    public UUID getId() {
        return id;
    }

    public Tweet getTweet() {
        return tweet;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MentionedUser user = (MentionedUser) o;
        return getId().equals(user.getId()) &&
                getTweet().equals(user.getTweet()) &&
                getUserId().equals(user.getUserId()) &&
                Objects.equals(getUsername(), user.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTweet(), getUserId(), getUsername());
    }
}
