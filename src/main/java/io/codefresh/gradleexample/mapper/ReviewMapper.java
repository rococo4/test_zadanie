package io.codefresh.gradleexample.mapper;

import io.codefresh.gradleexample.response.ReviewResponse;
import io.codefresh.gradleexample.store.entity.Review;
import io.codefresh.gradleexample.store.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId().toString())
                .createdAt(review.getCreatedAt())
                .description(review.getDescription())
                .build();
    }
}
