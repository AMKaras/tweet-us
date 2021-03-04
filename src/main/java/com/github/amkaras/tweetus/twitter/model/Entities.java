package com.github.amkaras.tweetus.twitter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {

    private final List<Hashtag> hashtags;
    private final List<Mention> mentions;

    @JsonCreator
    public Entities(@JsonProperty("hashtags") List<Hashtag> hashtags,
                    @JsonProperty("mentions") List<Mention> mentions) {
        this.hashtags = hashtags;
        this.mentions = mentions;
    }

    public List<Hashtag> getHashtags() {
        return hashtags;
    }

    public List<Mention> getMentions() {
        return mentions;
    }
}
