package com.github.amkaras.tweetus.entity.converter;

import com.github.amkaras.tweetus.entity.TweetState;

import javax.persistence.AttributeConverter;

public class TweetStateConverter implements AttributeConverter<TweetState, String> {

    @Override
    public String convertToDatabaseColumn(TweetState attribute) {
        switch (attribute) {
            case PENDING:
                return "PENDING";
            case UNAVAILABLE:
                return "UNAVAILABLE";
            case FETCHED:
                return "FETCHED";
            case ADDED:
                return "ADDED";
            default:
                throw new IllegalArgumentException("Unknown tweet state " + attribute);
        }
    }

    @Override
    public TweetState convertToEntityAttribute(String dbData) {
        switch (dbData) {
            case "PENDING":
                return TweetState.PENDING;
            case "UNAVAILABLE":
                return TweetState.UNAVAILABLE;
            case "FETCHED":
                return TweetState.FETCHED;
            case "ADDED":
                return TweetState.ADDED;
            default:
                throw new IllegalArgumentException("Unknown tweet state " + dbData);
        }
    }
}
