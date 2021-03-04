package com.github.amkaras.tweetus.entity.opinionfinder;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "opinion_finder_dictionary")
public class DictionaryEntry {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "analysis_id", referencedColumnName = "id", updatable = false)
    private OpinionFinderAnalysis analysis;

    @Column(name = "token")
    private String token;

    @Column(name = "mpqapolarity")
    private String polarity;

    public OpinionFinderAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(OpinionFinderAnalysis analysis) {
        this.analysis = analysis;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPolarity() {
        return polarity;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryEntry that = (DictionaryEntry) o;
        return id.equals(that.id) &&
                Objects.equals(getAnalysis(), that.getAnalysis()) &&
                Objects.equals(getToken(), that.getToken()) &&
                Objects.equals(getPolarity(), that.getPolarity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getAnalysis(), getToken(), getPolarity());
    }
}
