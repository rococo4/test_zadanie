package io.codefresh.gradleexample.response;

import io.codefresh.gradleexample.store.entity.ServiceType;
import io.codefresh.gradleexample.store.entity.TenderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenderResponse {
    private String id;

    private String name;

    private String description;

    private ServiceType serviceType;

    private TenderStatus status;

    private Integer version;

    private String organizationId;

    private LocalDateTime createdAt;

    private String creatorUsername;
}
