package com.github.amkaras.tweetus.external.twitter.repository;

import com.github.amkaras.tweetus.external.twitter.entity.Tweet;
import com.github.amkaras.tweetus.external.twitter.entity.TweetState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {

    List<Tweet> findByState(TweetState state, Pageable pageable);

    List<Tweet> findByBelongsToTrainingSet(boolean belongsToTrainingSet, Pageable pageable);

    List<Tweet> findByAnalyzedWithOpinionFinderOrderByRetweetCountDesc(boolean analyzedWithOpinionFinder, Pageable pageable);

    List<Tweet> findByBelongsToTrainingSetAndAnalyzedWithOpinionFinder(
            boolean belongsToTrainingSet, boolean analyzedWithOpinionFinder, Pageable pageable);

    @Query("select t.id from Tweet t where t.analyzedWithOpinionFinder = :analyzedWithOpinionFinder")
    List<String> findIdsByAnalyzedWithOpinionFinder(@Param("analyzedWithOpinionFinder") boolean analyzedWithOpinionFinder, Pageable pageable);

    @Modifying
    @Query("update Tweet t set t.belongsToTrainingSet = false where t.belongsToTrainingSet = true")
    void markAllAsNotBelongingToTrainingSet();

    @Modifying
    @Query("update Tweet t set t.belongsToTrainingSet = true where t.id in :ids")
    void markAsBelongingToTrainingSet(@Param("ids") List<String> ids);

    @Modifying
    @Query("update Tweet t set t.analyzedWithOpinionFinder = true where t.id in :ids")
    void markAsAnalyzedWithOpinionFinder(@Param("ids") List<String> ids);
}
