package io.codefresh.gradleexample.request;

import io.codefresh.gradleexample.store.entity.TenderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenderRequest {

    private String name;

    private String description;

    private String serviceType;
}
