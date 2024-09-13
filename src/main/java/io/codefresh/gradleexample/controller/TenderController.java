package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.request.CreateTenderRequest;
import io.codefresh.gradleexample.request.UpdateTenderRequest;
import io.codefresh.gradleexample.response.TenderResponse;
import io.codefresh.gradleexample.service.TenderService;
import io.codefresh.gradleexample.store.entity.TenderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TenderController {
    public static final String GET_TENDERS = "/api/tenders";
    public static final String CREATE_TENDER = "/api/tenders/new";
    public static final String GET_TENDERS_BY_USER = "/api/tenders/my";
    public static final String GET_TENDER_STATUS = "/api/tenders/{tenderId}/status";
    public static final String CHANGE_TENDER_STATUS = "/api/tenders/{tenderId}/status";
    public static final String EDIT_TENDER_BY_ID = "/api/tenders/{tenderId}/edit";
    public static final String ROLLBACK_TENDER_BY_ID = "/api/tenders/{tenderId}/rollback/{version}";

    private final TenderService tenderService;

    @GetMapping(GET_TENDERS)
    public List<TenderResponse> getTenders(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) List<String> service_type
    ) {
        return tenderService.getTenders(limit, offset, service_type);
    }

    @PostMapping(CREATE_TENDER)
    public TenderResponse createTender(@RequestBody CreateTenderRequest createTenderRequest) {
        return tenderService.createTender(createTenderRequest);
    }

    @GetMapping(GET_TENDERS_BY_USER)
    public List<TenderResponse> getTendersByUser(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) String username
    ) {
        return tenderService.getTenderByUser(username, limit, offset);
    }

    @GetMapping(GET_TENDER_STATUS)
    public String getTenderStatus(
            @PathVariable("tenderId") String tenderId,
            @RequestParam("username") String username) {
        return tenderService.getTenderStatusById(tenderId, username);
    }
    @PutMapping(CHANGE_TENDER_STATUS)
    public TenderResponse changeTenderStatus(
            @PathVariable("tenderId") String tenderId,
            @RequestParam String status,
            @RequestParam String username
    ) {
        return tenderService.updateTenderStatus(tenderId, status, username);
    }
    @PatchMapping(EDIT_TENDER_BY_ID)
    public TenderResponse editTender(
            @PathVariable("tenderId") String tenderId,
            @RequestParam String username,
            @RequestBody UpdateTenderRequest updateTenderRequest
    ) {
        System.out.println("Received PATCH request");
        return tenderService.updateTender(tenderId, username, updateTenderRequest);
    }

    @PutMapping(ROLLBACK_TENDER_BY_ID)
    public TenderResponse rollbackTender(
            @PathVariable("tenderId") String tenderId,
            @PathVariable("version") Integer version,
            @RequestParam String username
    ) {
        return tenderService.rollbackTenderById(tenderId, version, username);
    }



}
