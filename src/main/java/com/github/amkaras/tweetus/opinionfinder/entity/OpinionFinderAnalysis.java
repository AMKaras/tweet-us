package com.github.amkaras.tweetus.opinionfinder.entity;


import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "opinion_finder_analysis_results")
public class OpinionFinderAnalysis {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "entity")
    @Convert(converter = AnalysisEntityConverter.class)
    private AnalysisEntity entity;

    @Column(name = "entity_id")
    private String entityId;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PolarityClassifier> polarityClassifiers;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SubjectiveClue> subjectiveClues;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<DictionaryEntry> dictionary;

    public UUID getId() {
        return id;
    }

    public AnalysisEntity getEntity() {
        return entity;
    }

    public String getEntityId() {
        return entityId;
    }

    public Set<PolarityClassifier> getPolarityClassifiers() {
        return polarityClassifiers;
    }

    public Set<SubjectiveClue> getSubjectiveClues() {
        return subjectiveClues;
    }

    public void setEntity(AnalysisEntity entity) {
        this.entity = entity;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setPolarityClassifiers(Set<PolarityClassifier> polarityClassifiers) {
        this.polarityClassifiers = polarityClassifiers;
    }

    public void setSubjectiveClues(Set<SubjectiveClue> subjectiveClues) {
        this.subjectiveClues = subjectiveClues;
    }

    public Set<DictionaryEntry> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Set<DictionaryEntry> dictionary) {
        this.dictionary = dictionary;
    }
}
