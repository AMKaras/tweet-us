package com.github.amkaras.tweetus.external.opinionfinder.service;

import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;

import java.util.List;

public interface OpinionFinderAnalysisService {

    void save(Iterable<OpinionFinderAnalysis> analyses);

    List<OpinionFinderAnalysis> findByEntityIds(List<String> entityIds);
}
