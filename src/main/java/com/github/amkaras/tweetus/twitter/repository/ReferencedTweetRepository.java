package com.github.amkaras.tweetus.twitter.repository;

import com.github.amkaras.tweetus.twitter.entity.TweetReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferencedTweetRepository extends JpaRepository<TweetReference, UUID> {
}
