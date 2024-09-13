package io.codefresh.gradleexample.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String status;
    @NotBlank
    private String tenderId;
    @NotBlank
    private String organizationId;
    @NotBlank
    private String creatorUsername;
}
