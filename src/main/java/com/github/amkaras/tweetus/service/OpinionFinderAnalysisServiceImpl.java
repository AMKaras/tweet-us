package com.github.amkaras.tweetus.service;

import com.github.amkaras.tweetus.entity.opinionfinder.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.repository.OpinionFinderAnalysisRepository;
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
    public void save(OpinionFinderAnalysis analysis) {
        opinionFinderAnalysisRepository.save(analysis);
    }

    @Override
    public List<OpinionFinderAnalysis> findByEntityIds(List<String> entityIds) {
        return opinionFinderAnalysisRepository.findByEntityIdIn(entityIds);
    }
}
