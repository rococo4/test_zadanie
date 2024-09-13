package io.codefresh.gradleexample.mapper;

import io.codefresh.gradleexample.response.TenderResponse;
import io.codefresh.gradleexample.store.entity.Tender;
import io.codefresh.gradleexample.store.entity.TenderVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TenderMapper {
    public TenderResponse makeTenderResponse(Tender tender) {
        List<TenderVersion> versions = tender.getVersions();
        TenderVersion tenderVersion = null;
        for (TenderVersion version : versions) {
            if (version.isActive()) {
                tenderVersion = version;
                break;
            }
        }
        return TenderResponse.builder()
                .id(String.valueOf(tender.getId()))
                .name(tenderVersion.getName())
                .description(tenderVersion.getDescription())
                .serviceType(tenderVersion.getServiceType())
                .version(tenderVersion.getVersion())
                .status(tender.getStatus())
                .organizationId(String.valueOf(tender.getOrganization().getId()))
                .creatorUsername(tender.getCreator().getUsername())
                .createdAt(tender.getCreatedAt())
                .build();
    }

}
