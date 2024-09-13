package io.codefresh.gradleexample.store.repository;

import io.codefresh.gradleexample.store.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    @Query(value = "SELECT * FROM offer o WHERE o.author_id = :userId " +
            "LIMIT :limit OFFSET :offset ", nativeQuery = true)
    List<Offer> findAllByAuthor_Username(
            @Param("userId") UUID userId,
            @Param("limit") int limit,
            @Param("offset") int offset);


    @Query(value = "SELECT * FROM offer o WHERE (o.tender_id = :tenderId) " +
            "AND (o.offer_status = :status OR o.author_id = :userId) LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Offer> findAllByTender_Id(
            @Param("tenderId") UUID tenderId,
            @Param("userId") UUID userId,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
