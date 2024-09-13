package io.codefresh.gradleexample.response;

import io.codefresh.gradleexample.store.entity.TenderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenderStatusResponse {
    private TenderStatus status;
}
