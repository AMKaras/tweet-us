package com.github.amkaras.tweetus.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hashtag {

    private final String tag;

    @JsonCreator
    public Hashtag(@JsonProperty("tag") String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
