package com.github.amkaras.tweetus.common.algorithm.knn.model;

import com.github.amkaras.tweetus.common.model.ClassificationCategory;

import java.util.UUID;

public class Document {

    private final UUID id = UUID.randomUUID();
    private String humanName;
    private String content;
    private ClassificationCategory category;

    public UUID getId() {
        return id;
    }

    public String getHumanName() {
        return humanName;
    }

    public String getContent() {
        return content;
    }

    public ClassificationCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", humanName='" + humanName + '\'' +
                ", content='" + content + '\'' +
                ", category=" + category +
                '}';
    }

    public static final class Builder {

        private String humanName;
        private String content;
        private ClassificationCategory category;

        private Builder() {
        }

        public static Builder documentBuilder() {
            return new Builder();
        }

        public Builder withHumanName(String humanName) {
            this.humanName = humanName;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withCategory(ClassificationCategory category) {
            this.category = category;
            return this;
        }

        public Document build() {
            Document document = new Document();
            document.category = this.category;
            document.humanName = this.humanName;
            document.content = this.content;
            return document;
        }
    }
}
