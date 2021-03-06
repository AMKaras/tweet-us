package com.github.amkaras.tweetus.external.opinionfinder.repository;

import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpinionFinderAnalysisRepository extends JpaRepository<OpinionFinderAnalysis, UUID> {
    List<OpinionFinderAnalysis> findByEntityIdIn(List<String> entityIds);
}
