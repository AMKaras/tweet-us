package com.github.amkaras.tweetus.entity.converter;

import com.github.amkaras.tweetus.entity.opinionfinder.AnalysisEntity;

import javax.persistence.AttributeConverter;

public class AnalysisEntityConverter implements AttributeConverter<AnalysisEntity, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisEntity attribute) {
        switch (attribute) {
            case TWEET:
                return "TWEET";
            case COLLECTION:
                return "COLLECTION";
            case AUTHOR:
                return "AUTHOR";
            default:
                throw new IllegalArgumentException("Unknown analysis entity " + attribute);
        }
    }

    @Override
    public AnalysisEntity convertToEntityAttribute(String dbData) {
        switch (dbData) {
            case "TWEET":
                return AnalysisEntity.TWEET;
            case "COLLECTION":
                return AnalysisEntity.COLLECTION;
            case "AUTHOR":
                return AnalysisEntity.AUTHOR;
            default:
                throw new IllegalArgumentException("Unknown analysis entity " + dbData);
        }
    }
}
