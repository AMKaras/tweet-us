package com.github.amkaras.tweetus.twitter.entity;

import com.github.amkaras.tweetus.twitter.entity.ReferenceType;

import javax.persistence.AttributeConverter;

public class ReferenceTypeConverter implements AttributeConverter<ReferenceType, String> {

    @Override
    public String convertToDatabaseColumn(ReferenceType attribute) {
        switch (attribute) {
            case RETWEETED:
                return "RETWEETED";
            case QUOTED:
                return "QUOTED";
            case REPLIED_TO:
                return "REPLIED_TO";
            default:
                throw new IllegalArgumentException("Unknown reference type " + attribute);
        }
    }

    @Override
    public ReferenceType convertToEntityAttribute(String dbData) {
        switch (dbData) {
            case "RETWEETED":
                return ReferenceType.RETWEETED;
            case "QUOTED":
                return ReferenceType.QUOTED;
            case "REPLIED_TO":
                return ReferenceType.REPLIED_TO;
            default:
                throw new IllegalArgumentException("Unknown reference type " + dbData);
        }
    }
}
