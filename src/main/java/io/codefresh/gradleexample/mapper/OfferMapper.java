package io.codefresh.gradleexample.mapper;

import io.codefresh.gradleexample.response.OfferResponse;
import io.codefresh.gradleexample.store.entity.Offer;
import io.codefresh.gradleexample.store.entity.OfferVersion;
import io.codefresh.gradleexample.store.entity.TenderVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OfferMapper {
    public OfferResponse makeOfferResponse(Offer offer) {
        List<OfferVersion> versions = offer.getVersions();
        OfferVersion offerVersion = versions.get(0);
        for (OfferVersion version : versions) {
            if (version.isActive()) {
                offerVersion = version;
                break;
            }
        }
        return OfferResponse.builder()
                .id(offer.getId().toString())
                .name(offerVersion.getName())
                .description(offerVersion.getDescription())
                .status(offer.getOfferStatus())
                .authorType(offer.getAuthorType())
                .tenderId(offer.getTender().getId().toString())
                .authorId(offer.getAuthor().getId().toString())
                .version(offerVersion.getVersion())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
