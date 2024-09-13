package io.codefresh.gradleexample.store.repository;

import io.codefresh.gradleexample.store.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @Query(value = "SELECT * FROM Review r WHERE r.reviewee_id = :id LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Review> findByReviewee_Id(
            @Param("id") UUID id,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
