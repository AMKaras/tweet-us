package com.github.amkaras.tweetus.external.opinionfinder.service;

import com.github.amkaras.tweetus.external.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.external.opinionfinder.repository.OpinionFinderAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
@Service
public class OpinionFinderAnalysisServiceImpl implements OpinionFinderAnalysisService {

    private final OpinionFinderAnalysisRepository opinionFinderAnalysisRepository;

    @Autowired
    public OpinionFinderAnalysisServiceImpl(OpinionFinderAnalysisRepository opinionFinderAnalysisRepository) {
        this.opinionFinderAnalysisRepository = opinionFinderAnalysisRepository;
    }

    @Override
    public void save(Iterable<OpinionFinderAnalysis> analyses) {
        opinionFinderAnalysisRepository.saveAll(analyses);
    }

    @Override
    public List<OpinionFinderAnalysis> findByEntityIds(List<String> entityIds) {
        return opinionFinderAnalysisRepository.findByEntityIdIn(entityIds);
    }
}
