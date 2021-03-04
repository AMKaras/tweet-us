package com.github.amkaras.tweetus.entity.opinionfinder;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "opinion_finder_polarity_classifiers")
public class PolarityClassifier {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "analysis_id", referencedColumnName = "id", updatable = false)
    private OpinionFinderAnalysis analysis;

    @Column(name = "score")
    private String score;

    @Column(name = "count")
    private Integer count;

    public UUID getId() {
        return id;
    }

    public OpinionFinderAnalysis getAnalysis() {
        return analysis;
    }

    public String getScore() {
        return score;
    }

    public Integer getCount() {
        return count;
    }

    public void setAnalysis(OpinionFinderAnalysis analysis) {
        this.analysis = analysis;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolarityClassifier that = (PolarityClassifier) o;
        return getId().equals(that.getId()) &&
                getAnalysis().equals(that.getAnalysis()) &&
                Objects.equals(getScore(), that.getScore()) &&
                Objects.equals(getCount(), that.getCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAnalysis(), getScore(), getCount());
    }
}
