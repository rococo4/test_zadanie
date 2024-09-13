package io.codefresh.gradleexample.request;

import io.codefresh.gradleexample.store.entity.TenderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenderRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "ServiceType cannot be blank")
    private String serviceType;

    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotBlank(message = "OrganizationId cannot be blank")
    private String organizationId;

    @NotBlank(message = "CreatorUsername cannot be blank")
    private String creatorUsername;
}
