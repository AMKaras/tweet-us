package com.github.amkaras.tweetus.service;

import com.github.amkaras.tweetus.entity.opinionfinder.OpinionFinderAnalysis;

import java.util.List;

public interface OpinionFinderAnalysisService {

    void save(OpinionFinderAnalysis analysis);

    List<OpinionFinderAnalysis> findByEntityIds(List<String> entityIds);
}
