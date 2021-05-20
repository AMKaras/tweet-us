package com.github.amkaras.tweetus.external.twitter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mention {

    private final String username;

    @JsonCreator
    public Mention(@JsonProperty("username") String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
