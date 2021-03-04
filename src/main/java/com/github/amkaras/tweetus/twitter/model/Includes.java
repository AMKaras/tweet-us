package com.github.amkaras.tweetus.twitter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Includes {

    private final List<User> users;
    private final List<Data> tweets;

    @JsonCreator
    public Includes(@JsonProperty("users") List<User> users,
                    @JsonProperty("tweets") List<Data> tweets) {
        this.users = users;
        this.tweets = tweets;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Data> getTweets() {
        return tweets;
    }
}
