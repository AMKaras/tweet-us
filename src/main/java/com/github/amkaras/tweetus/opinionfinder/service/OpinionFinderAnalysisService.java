package com.github.amkaras.tweetus.opinionfinder.service;

import com.github.amkaras.tweetus.opinionfinder.entity.OpinionFinderAnalysis;

import java.util.List;

public interface OpinionFinderAnalysisService {

    void save(OpinionFinderAnalysis analysis);

    void save(Iterable<OpinionFinderAnalysis> analyses);

    List<OpinionFinderAnalysis> findByEntityIds(List<String> entityIds);
}
