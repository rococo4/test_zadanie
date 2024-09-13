package io.codefresh.gradleexample.response;

import io.codefresh.gradleexample.store.entity.Offer;
import io.codefresh.gradleexample.store.entity.OfferAuthorType;
import io.codefresh.gradleexample.store.entity.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    private String id;
    private String name;
    private String description;
    private OfferStatus status;
    private String tenderId;
    private OfferAuthorType authorType;
    private String authorId;
    private Integer version;
    private LocalDateTime createdAt;
}
