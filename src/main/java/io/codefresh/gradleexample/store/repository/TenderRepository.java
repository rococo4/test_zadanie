package io.codefresh.gradleexample.store.repository;

import io.codefresh.gradleexample.store.entity.ServiceType;
import io.codefresh.gradleexample.store.entity.Tender;
import io.codefresh.gradleexample.store.entity.TenderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderRepository extends JpaRepository<Tender, UUID> {

    @Query(
            value = "SELECT * FROM tender t WHERE t.status = :tenderStatus " +
                    "AND t.service_type IN :serviceType " +
                    "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Tender> findTendersByServiceTypeWithLimitOffset(
            @Param("serviceType") List<String> serviceType,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("tenderStatus") String tenderStatus
    );
    List<Tender> findAllByStatus(TenderStatus status);

    @Query(value = "SELECT * FROM tender t WHERE t.creator_id = :creatorId LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Tender> findByCreatorIdWithLimitOffset(
            @Param("creatorId") UUID creatorId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    List<Tender> findAllByCreator_Username(String username);
}
