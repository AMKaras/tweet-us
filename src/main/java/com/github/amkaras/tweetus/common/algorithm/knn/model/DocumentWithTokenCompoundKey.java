package com.github.amkaras.tweetus.common.algorithm.knn.model;

import java.util.Objects;

public class DocumentWithTokenCompoundKey {

    private final Document document;
    private final String token;

    public DocumentWithTokenCompoundKey(Document document, String token) {
        this.document = document;
        this.token = token;
    }

    public Document getDocument() {
        return document;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentWithTokenCompoundKey that = (DocumentWithTokenCompoundKey) o;
        return getDocument().equals(that.getDocument()) &&
                getToken().equals(that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDocument(), getToken());
    }

    @Override
    public String toString() {
        return "DocumentWithTokenCompoundKey{" +
                "Document={humanName='" + document.getHumanName() +
                "', id='" + document.getId() +
                "'}, token='" + token + '\'' +
                '}';
    }
}
