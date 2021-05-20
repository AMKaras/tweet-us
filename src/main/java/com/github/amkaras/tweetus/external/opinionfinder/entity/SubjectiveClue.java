package com.github.amkaras.tweetus.external.opinionfinder.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "opinion_finder_subjective_clues")
public class SubjectiveClue {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "analysis_id", referencedColumnName = "id", updatable = false)
    private OpinionFinderAnalysis analysis;

    @Column(name = "mpqapolarity")
    private String polarity;

    @Column(name = "count")
    private Integer count;

    public UUID getId() {
        return id;
    }

    public OpinionFinderAnalysis getAnalysis() {
        return analysis;
    }

    public String getPolarity() {
        return polarity;
    }

    public Integer getCount() {
        return count;
    }

    public void setAnalysis(OpinionFinderAnalysis analysis) {
        this.analysis = analysis;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectiveClue that = (SubjectiveClue) o;
        return getId().equals(that.getId()) &&
                getAnalysis().equals(that.getAnalysis()) &&
                Objects.equals(getPolarity(), that.getPolarity()) &&
                Objects.equals(getCount(), that.getCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAnalysis(), getPolarity(), getCount());
    }
}
