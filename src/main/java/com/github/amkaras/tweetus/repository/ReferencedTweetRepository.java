package com.github.amkaras.tweetus.repository;

import com.github.amkaras.tweetus.entity.TweetReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferencedTweetRepository extends JpaRepository<TweetReference, UUID> {
}
