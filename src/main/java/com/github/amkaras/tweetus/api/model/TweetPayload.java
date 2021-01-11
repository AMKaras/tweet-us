package com.github.amkaras.tweetus.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetPayload {

    private final Data data;
    private final Includes includes;

    @JsonCreator
    public TweetPayload(@JsonProperty("data") Data data,
                        @JsonProperty("includes") Includes includes) {
        this.data = data;
        this.includes = includes;
    }

    public Data getData() {
        return data;
    }

    public Includes getIncludes() {
        return includes;
    }
}
