package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.exception.BadRequestException;
import io.codefresh.gradleexample.exception.ForbiddenException;
import io.codefresh.gradleexample.exception.NotFoundException;
import io.codefresh.gradleexample.exception.UnauthorizedException;
import io.codefresh.gradleexample.mapper.TenderMapper;
import io.codefresh.gradleexample.request.CreateTenderRequest;
import io.codefresh.gradleexample.request.UpdateTenderRequest;
import io.codefresh.gradleexample.response.TenderResponse;
import io.codefresh.gradleexample.store.entity.*;
import io.codefresh.gradleexample.store.repository.EmployeeRepository;
import io.codefresh.gradleexample.store.repository.OrganizationRepository;
import io.codefresh.gradleexample.store.repository.OrganizationResponsibleRepository;
import io.codefresh.gradleexample.store.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TenderService {
    private final TenderRepository tenderRepository;
    private final TenderMapper tenderMapper;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationResponsibleRepository organizationResponsibleRepository;

    public List<TenderResponse> getTenders(int limit, int offset, List<String> serviceType) {
        if (limit > 50 || limit < 0) {
            throw new BadRequestException("limit must be in range [0,50]");
        }
        if (offset < 0) {
            throw new BadRequestException("offset must be >= 0");
        }
        List<ServiceType> listServiceTypes = new ArrayList<>();
        if (!serviceType.isEmpty()) {
            for (String s : serviceType) {
                try {
                    listServiceTypes.add(ServiceType.valueOf(s));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid service type: " + s);
                }
            }
        } else {
            listServiceTypes.addAll(Arrays.asList(ServiceType.values()));
        }
        return tenderRepository.
                findTendersByServiceTypeWithLimitOffset(
                        listServiceTypes.stream().map(Enum::name).toList(),
                        limit,
                        offset,
                        TenderStatus.Published.name())
                .stream().map(tenderMapper::makeTenderResponse).toList();

    }

    public TenderResponse createTender(CreateTenderRequest createTenderRequest) {
        Employee creator = employeeRepository.findByUsername(createTenderRequest.getCreatorUsername())
                .orElseThrow(() -> new UnauthorizedException("Employee not found"));
        Organization organization = organizationRepository.findById(
                UUID.fromString(createTenderRequest.getOrganizationId()))
                .orElseThrow(() -> new UnauthorizedException("Organization not found"));

        if  (createTenderRequest.getName().length() > 100) {
            throw new BadRequestException("Tender name is too long");
        }
        if  (createTenderRequest.getDescription().length() > 500) {
            throw new BadRequestException("Tender description is too long");
        }
        if (createTenderRequest.getOrganizationId().length() > 100) {
            throw new BadRequestException("Organization id is too long");
        }
        ServiceType serviceType;
        try {
            serviceType = ServiceType.valueOf(createTenderRequest.getServiceType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid service type: " + createTenderRequest.getServiceType());
        }

        if (!havePermissionForTender(creator, organization)) {
            throw new ForbiddenException("You are not allowed");
        }
        TenderStatus tenderStatus;
        try {
            tenderStatus = TenderStatus.valueOf(createTenderRequest.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid service type: " + createTenderRequest.getServiceType());
        }
        TenderVersion tenderVersion = TenderVersion.builder()
                .active(true)
                .version(1)
                .description(createTenderRequest.getDescription())
                .serviceType(serviceType)
                .name(createTenderRequest.getName())
                .build();
        List<TenderVersion> versions = new ArrayList<>();
        versions.add(tenderVersion);
        Tender tender = Tender.builder()
                .versions(versions)
                .status(tenderStatus)
                .organization(organization)
                .creator(creator)
                .build();
        Tender savedTender = tenderRepository.saveAndFlush(tender);
        return tenderMapper.makeTenderResponse(savedTender);
    }

    private boolean havePermissionForTender(Employee creator, Organization organization) {
        List<OrganizationResponsible> organizationsForUser =
                organizationResponsibleRepository.findAllByEmployee_Id(creator.getId());
        boolean isAllowed = false;
        for (OrganizationResponsible organizationResponsible : organizationsForUser) {
            if (organizationResponsible.getOrganization().getId().equals(organization.getId())) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    public List<TenderResponse> getTenderByUser(String username,Integer limit, Integer offset) {
        if (limit > 50 || limit < 0) {
            throw new BadRequestException("limit must be in range [0,50]");
        }
        if (offset < 0) {
            throw new BadRequestException("offset must be >= 0");
        }
        UUID userId = employeeRepository.findByUsername(username).orElseThrow(
                () -> new UnauthorizedException(String.format("User with id %s not found", username))).getId();

        return tenderRepository.findByCreatorIdWithLimitOffset(userId,limit, offset).stream()
                .map(tenderMapper::makeTenderResponse).toList();
    }

    public String getTenderStatusById(String tenderId, String username) {
        Tender tender = tenderRepository.findById(UUID.fromString(tenderId)).orElseThrow(() ->
                new BadRequestException(String.format("Tender with id %s not found", tenderId)));
        if (tender.getStatus() != TenderStatus.Published) {
           Employee user = employeeRepository.findByUsername(username).orElseThrow(()
                   -> new UnauthorizedException(String.format("User with id %s not found", username)));
           if (!havePermissionForTender(user,tender.getOrganization())) {
               throw new ForbiddenException("You are not allowed");
           }
        }
        return tender.getStatus().name();
    }

    public TenderResponse updateTenderStatus(String tenderId, String status, String username) {
        TenderStatus tenderStatus;
        try {
            tenderStatus = TenderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
        Tender tenderToChange = tenderRepository.findById(UUID.fromString(tenderId))
                .orElseThrow(() -> new NotFoundException(String.format("Tender with id %s not found", tenderId)));
        Employee user = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(String.format("User with id %s not found", username)));
        if (!havePermissionForTender(user, tenderToChange.getOrganization())) {
            throw new ForbiddenException("You are not allowed");
        }
        tenderToChange.setStatus(tenderStatus);
        return tenderMapper.makeTenderResponse(tenderRepository.saveAndFlush(tenderToChange));

    }

    public TenderResponse updateTender(String tenderId, String username , UpdateTenderRequest updateTenderRequest) {
        Tender tenderToUpdate = tenderRepository.findById(UUID.fromString(tenderId))
                .orElseThrow(() -> new NotFoundException("Tender not found"));
        Employee user = employeeRepository.findByUsername(username).orElseThrow(
                () -> new UnauthorizedException(String.format("User with id %s not found", username)));
        if (!havePermissionForTender(user, tenderToUpdate.getOrganization())) {
            throw new ForbiddenException("You are not allowed");
        }
        TenderVersion tenderVersion = new TenderVersion();
        if (updateTenderRequest.getName() != null) {
            if  (updateTenderRequest.getName().length() > 100) {
                throw new BadRequestException("Tender name is too long");
            }
            tenderVersion.setName(updateTenderRequest.getName());
        }
        if (updateTenderRequest.getDescription() != null) {
            if  (updateTenderRequest.getDescription().length() > 500) {
                throw new BadRequestException("Tender description is too long");
            }
            tenderVersion.setDescription(updateTenderRequest.getDescription());
        }
        if (updateTenderRequest.getServiceType() != null) {
            ServiceType serviceType;
            try {
                serviceType = ServiceType.valueOf(updateTenderRequest.getServiceType());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid service type: " + updateTenderRequest.getServiceType());
            }
            tenderVersion.setServiceType(serviceType);
        }
        tenderVersion.setVersion(tenderToUpdate.getVersions().size() + 1);
        tenderVersion.setActive(true);
        List<TenderVersion> tenderVersions = tenderToUpdate.getVersions();

        for (TenderVersion tenderV : tenderToUpdate.getVersions()) {
            if (tenderV.isActive()) {
                tenderV.setActive(false);
            }
        }
        tenderVersions.add(tenderVersion);
        tenderToUpdate.setVersions(tenderVersions);

        return tenderMapper.makeTenderResponse(tenderRepository.saveAndFlush(tenderToUpdate));
    }

    public TenderResponse rollbackTenderById(String tenderId, Integer version, String username) {
        if (version < 1) {
            throw new BadRequestException("version must be >= 1");
        }
        Tender tenderToUpdate = tenderRepository.findById(UUID.fromString(tenderId))
                .orElseThrow(() -> new NotFoundException("Tender not found"));
        Employee user = employeeRepository.findByUsername(username).orElseThrow(
                () -> new UnauthorizedException(String.format("User with id %s not found", username))
        );
        if (!havePermissionForTender(user, tenderToUpdate.getOrganization())) {
            throw new ForbiddenException("You are not allowed");
        }
        List<TenderVersion> allVersions = tenderToUpdate.getVersions();
        boolean contains = false;
        for (TenderVersion tenderV : allVersions) {
            if (Objects.equals(tenderV.getVersion(), version)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            throw new NotFoundException("Version not found");
        }
        allVersions.forEach((e) -> e.setActive(false));
        allVersions.forEach((e) -> {
            if (Objects.equals(e.getVersion(), version)) {
                e.setActive(true);
            }
        });
        tenderToUpdate.setVersions(allVersions);
        return tenderMapper.makeTenderResponse(tenderRepository.saveAndFlush(tenderToUpdate));
    }


}
