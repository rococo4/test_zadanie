package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.request.CreateOfferRequest;
import io.codefresh.gradleexample.request.EditOfferRequest;
import io.codefresh.gradleexample.response.OfferResponse;
import io.codefresh.gradleexample.response.ReviewResponse;
import io.codefresh.gradleexample.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OfferController {
    public static final String CREATE_BID = "api/bids/new";
    public static final String GET_BIDS = "api/bids/my";
    public static final String GET_BIDS_FOR_TENDER = "api/bids/{tenderId}/list";
    public static final String GET_BID_STATUS = "api/bids/{bidId}/status";
    public static final String CHANGE_BID_STATUS = "api/bids/{bidId}/status";
    public static final String EDIT_BID = "api/bids/{bidId}/edit";
    public static final String SUBMIT_DECISION = "api/bids/{bidId}/submit_decision";
    public static final String SEND_FEEDBACK = "api/bids/{bidId}/feedback";
    public static final String ROLLBACK_BID = "api/bids/{bidId}/rollback/{version}";
    public static final String GET_REVIEWS = "api/bids/{tenderId}/reviews";
    private final OfferService offerService;

    @GetMapping("/api/ping")
    public String ping() {
        return null;
    }

    @PostMapping(CREATE_BID)
    public OfferResponse createBid(@RequestBody CreateOfferRequest createOfferRequest) {
        return offerService.createBid(createOfferRequest);
    }
    @GetMapping(GET_BIDS)
    public List<OfferResponse> getBids(
            @RequestParam Integer limit,
            @RequestParam Integer offset,
            @RequestParam String username
    ) {
        return offerService.getBids(limit, offset, username);

    }
    @GetMapping(GET_BIDS_FOR_TENDER)
    public List<OfferResponse> getBidsForTender(
            @PathVariable String tenderId,
            @RequestParam String username,
            @RequestParam Integer limit,
            @RequestParam Integer offset
    ) {
        return offerService.getBidsForTender(tenderId, username, limit, offset);

    }
    @GetMapping(GET_BID_STATUS)
    public String getBidStatus(
            @PathVariable String bidId,
            @RequestParam String username) {
        return offerService.getBidStatus(bidId, username);

    }
    @PutMapping(CHANGE_BID_STATUS)
    public OfferResponse changeBidStatus(
            @PathVariable String bidId,
            @RequestParam String status,
            @RequestParam String username
    ) {
        return offerService.changeBidStatus(bidId, status, username);

    }
    @PatchMapping(EDIT_BID)
    public OfferResponse editBid(
            @PathVariable String bidId,
            @RequestParam String username,
            @RequestBody EditOfferRequest editOfferRequest
    ) {
        return offerService.editBid(bidId, username, editOfferRequest);

    }
    @PutMapping(SUBMIT_DECISION)
    public OfferResponse submitDecision(
            @PathVariable String bidId,
            @RequestParam String decision,
            @RequestParam String username
    ) {
        return offerService.submitDecision(bidId, decision, username);

    }
    @PutMapping(SEND_FEEDBACK)
    public OfferResponse sendFeedback(
            @PathVariable String bidId,
            @RequestParam String bidFeedback,
            @RequestParam String username
    ) {
        return offerService.sendFeedback(bidId, bidFeedback, username);

    }
    @PutMapping(ROLLBACK_BID)
    public OfferResponse rollbackBid(
            @PathVariable("bidId") String bidId,
            @PathVariable("version") Integer version,
            @RequestParam String username
    ) {
        return offerService.rollbackBid(bidId, version, username);

    }
    @GetMapping(GET_REVIEWS)
    public List<ReviewResponse> getReviews(
            @PathVariable String tenderId,
            @RequestParam String authorUsername,
            @RequestParam String requesterUsername,
            @RequestParam Integer limit,
            @RequestParam Integer offset
    ) {
        return offerService.getReviews(tenderId, authorUsername, requesterUsername, limit, offset);

    }
}
