package com.github.amkaras.tweetus.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private final String id;
    private final String username;
    private final String name;
    private final LocalDateTime createdAt;

    @JsonCreator
    public User(@JsonProperty("id") String id,
                @JsonProperty("username") String username,
                @JsonProperty("name") String name,
                @JsonProperty("created_at") LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
