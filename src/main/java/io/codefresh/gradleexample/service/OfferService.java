package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.exception.BadRequestException;
import io.codefresh.gradleexample.exception.ForbiddenException;
import io.codefresh.gradleexample.exception.NotFoundException;
import io.codefresh.gradleexample.exception.UnauthorizedException;
import io.codefresh.gradleexample.mapper.OfferMapper;
import io.codefresh.gradleexample.mapper.ReviewMapper;
import io.codefresh.gradleexample.request.CreateOfferRequest;
import io.codefresh.gradleexample.request.EditOfferRequest;
import io.codefresh.gradleexample.response.OfferResponse;
import io.codefresh.gradleexample.response.ReviewResponse;
import io.codefresh.gradleexample.store.entity.*;
import io.codefresh.gradleexample.store.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferService {
    private final TenderRepository tenderRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final OfferRepository offerRepository;
    private final OrganizationResponsibleRepository organizationResponsibleRepository;
    private final OfferMapper offerMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public OfferResponse createBid(CreateOfferRequest createOfferRequest) {
        if (createOfferRequest.getName().length() > 100) {
            throw new BadRequestException("Name is too long");
        }
        if (createOfferRequest.getDescription().length() > 500) {
            throw new BadRequestException("Description is too long");
        }
        OfferStatus offerStatus = ValidateOfferStatus(createOfferRequest.getStatus());
        Tender tender = ValidateTender(createOfferRequest.getTenderId(), new NotFoundException(
                String.format("Tender with id %s not found", createOfferRequest.getTenderId())
        ));
        Organization organization = ValidateOrganization(createOfferRequest.getOrganizationId(), new NotFoundException(
                String.format("Organization with id %s not found", createOfferRequest.getOrganizationId())
        ));
        Employee user = ValidateEmployee(createOfferRequest.getCreatorUsername(), new NotFoundException(
                String.format("Username %s not found", createOfferRequest.getCreatorUsername())
        ));
        Optional<OrganizationResponsible> organizationUserIsResponsible =
                organizationResponsibleRepository.findByEmployee_Id(user.getId());
        OfferAuthorType offerAuthorType = OfferAuthorType.User;
        if (organizationUserIsResponsible.isPresent()) {
            offerAuthorType = OfferAuthorType.Organization;
        }
        OfferVersion offerVersion = OfferVersion.builder()
                .version(1)
                .active(true)
                .name(createOfferRequest.getName())
                .description(createOfferRequest.getDescription())
                .build();
        List<OfferVersion> offerVersions = new ArrayList<>();
        offerVersions.add(offerVersion);
        return offerMapper.makeOfferResponse(
                offerRepository.saveAndFlush(
                        Offer.builder()
                                .versions(offerVersions)
                                .offerStatus(offerStatus)
                                .tender(tender)
                                .organization(organization)
                                .author(user)
                                .authorType(offerAuthorType)
                                .build()
                ));


    }

    public List<OfferResponse> getBids(Integer limit, Integer offset, String username) {
        Employee user = ValidateEmployee(username, new UnauthorizedException("User not found"));
        return offerRepository
                .findAllByAuthor_Username(user.getId(), limit, offset)
                .stream()
                .map(offerMapper::makeOfferResponse)
                .toList();

    }

    public List<OfferResponse> getBidsForTender(String tenderId, String username, Integer limit, Integer offset) {
        Employee user = ValidateEmployee(username, new UnauthorizedException(
                String.format("Username %s not found", username)
        ));
        Tender tender = ValidateTender(tenderId, new NotFoundException(
                String.format("Tender with id %s not found", tenderId)
        ));
        List<Organization> userInOrganizations = organizationResponsibleRepository
                .findByEmployee_Id(user.getId())
                .map(OrganizationResponsible::getOrganization)
                .stream().toList();
        // понять в какой организации пользователь, по организации найти всех отвественных, им давать разрешение
        // на просмотр всех предложений
        List<UUID> allResponsibleForOrganizationOffer = new ArrayList<>();
        if (!userInOrganizations.isEmpty()) {
            allResponsibleForOrganizationOffer = userInOrganizations.stream().map((e) -> {
                return organizationResponsibleRepository
                        .findAllByOrganization_Id(
                                e.getId()
                        ).stream().map(OrganizationResponsible::getEmployee).map(Employee::getId).toList();
            }).flatMap(List::stream).toList();
        }

        // если паблишед, то найти пользователей которые ответственны за организацию(тендер), и им показывать только паблишед
        List<UUID> allResponsibleForOrganizationTender = userInOrganizations.stream().map((e) -> {
            return organizationResponsibleRepository
                    .findAllByOrganization_Id(
                            e.getId()
                    ).stream().map(OrganizationResponsible::getEmployee).map(Employee::getId).toList();
        }).flatMap(List::stream).toList();

        List<Offer> allOffersByTenderId = offerRepository
                .findAllByTender_Id(UUID.fromString(tenderId), user.getId(), OfferStatus.Published.name(), limit, offset);
        if (allOffersByTenderId.isEmpty()) {
            throw new NotFoundException("No bids found");
        }
        List<Offer> finalAnswer = new ArrayList<>();
        for (Offer offer : allOffersByTenderId) {
            if (allResponsibleForOrganizationOffer.contains(user.getId())) {
                finalAnswer.add(offer);
            } else if (allResponsibleForOrganizationTender.contains(user.getId())) {
                if (offer.getOfferStatus() == OfferStatus.Published) {
                    finalAnswer.add(offer);
                }
            } else if (offer.getAuthor().getId().equals(user.getId())) {
                finalAnswer.add(offer);
            }
        }
        if (finalAnswer.isEmpty()) {
            throw new ForbiddenException("You are not allowed to watch offers");
        }
        return finalAnswer.stream().map(offerMapper::makeOfferResponse).toList();
    }

    public String getBidStatus(String bidId, String username) {
        // могут смотреть если паблишед, все от организаций для тендера и для предложения
        // если не паблишед, то только для той организации, у которой оффер
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));

        Organization organization = offer.getOrganization();
        List<UUID> allResponsibleForOrganizationOffer =
                organizationResponsibleRepository.findAllByOrganization_Id(organization.getId())
                        .stream()
                        .map(OrganizationResponsible::getEmployee)
                        .map(Employee::getId).toList();

        Tender tender = offer.getTender();
        List<UUID> allResponsibleForOrganizationTender =
                organizationResponsibleRepository
                        .findAllByOrganization_Id(organization.getId())
                        .stream()
                        .map(OrganizationResponsible::getEmployee)
                        .map(Employee::getId).toList();

        if (allResponsibleForOrganizationOffer.contains(user.getId())) {
            return offer.getOfferStatus().toString();
        }
        if (allResponsibleForOrganizationTender.contains(user.getId())
                && offer.getOfferStatus() == OfferStatus.Published) {
            return offer.getOfferStatus().toString();
        }
        if (offer.getAuthor().getId().equals(user.getId())) {
            return offer.getOfferStatus().toString();
        }
        throw new ForbiddenException("You are not allowed to watch offers");

    }

    public OfferResponse changeBidStatus(String bidId, String status, String username) {
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));
        OfferStatus offerStatus = ValidateOfferStatus(status);
        Organization organization = offer.getOrganization();
        List<UUID> allResponsibleForOrganizationOffer =
                organizationResponsibleRepository.findAllByOrganization_Id(organization.getId())
                        .stream()
                        .map(OrganizationResponsible::getEmployee)
                        .map(Employee::getId).toList();
        if (allResponsibleForOrganizationOffer.contains(user.getId())) {
            offer.setOfferStatus(offerStatus);
            return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
        }
        if (offer.getAuthor().getId().equals(user.getId())) {
            offer.setOfferStatus(offerStatus);
            return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
        }
        throw new ForbiddenException("You are not allowed to watch offers");
    }

    public OfferResponse editBid(String bidId, String username, EditOfferRequest editOfferRequest) {
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));
        Organization organization = offer.getOrganization();
        List<UUID> allResponsibleForOrganizationOffer =
                organizationResponsibleRepository.findAllByOrganization_Id(organization.getId())
                        .stream()
                        .map(OrganizationResponsible::getEmployee)
                        .map(Employee::getId).toList();
        OfferVersion newOfferVersion = new OfferVersion();
        if (editOfferRequest.getName() != null) {
            if (editOfferRequest.getName().length() > 100) {
                throw new BadRequestException("Name is too long");
            }
            newOfferVersion.setName(editOfferRequest.getName());
        }
        if (editOfferRequest.getDescription() != null) {
            if (editOfferRequest.getDescription().length() > 500) {
                throw new BadRequestException("Description is too long");
            }
            newOfferVersion.setDescription(editOfferRequest.getDescription());
        }
        newOfferVersion.setActive(true);
        newOfferVersion.setVersion(offer.getVersions().size() + 1);
        List<OfferVersion> oldOfferVersions = offer.getVersions();
        oldOfferVersions.forEach((e) -> {
            e.setActive(false);
        });
        oldOfferVersions.add(newOfferVersion);
        offer.setVersions(oldOfferVersions);
        if (allResponsibleForOrganizationOffer.contains(user.getId())) {
            return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
        }
        if (offer.getAuthor().getId().equals(user.getId())) {
            return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
        }
        throw new ForbiddenException("You are not allowed to watch offers");
    }

    public OfferResponse submitDecision(String bidId, String decisionStr, String username) {
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));
        DecisionType decisionType = ValidateDecisionType(decisionStr);
        List<UUID> allResponsibleForOrganization = organizationResponsibleRepository
                .findAllByOrganization_Id(offer.getTender().getOrganization().getId())
                .stream()
                .map(OrganizationResponsible::getEmployee)
                .map(Employee::getId).toList();
        if (!allResponsibleForOrganization.contains(user.getId())) {
            throw new ForbiddenException("You are not allowed to watch offers");
        }
        List<Decision> decisions = offer.getDecisions();
        Decision newDecision = Decision.builder()
                .decisionMaker(user)
                .decision(decisionType)
                .build();
        decisions.add(newDecision);
        offer.setDecisions(decisions);
        int minVotes = Math.min(allResponsibleForOrganization.size(), 3);
//        if (decisionTypes.stream().map(DecisionType::getDecisionMaker).map(Employee::getId).toList().contains(user.getId())) {
//            throw new ForbiddenException("You have already made a decision");
//        }
        for (Decision d : decisions) {
            if (d.getDecision() == DecisionType.Rejected) {
                offer.setOfferStatus(OfferStatus.Rejected);
                return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
            }
        }
        if (decisions.size() >= minVotes) {
            offer.setOfferStatus(OfferStatus.Approved);
        }
        return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
    }

    public OfferResponse sendFeedback(String bidId, String feedback, String username) {
        if (feedback.length() > 1000) {
            throw new BadRequestException("Feedback is too long");
        }
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));
        List<UUID> allResponsibleForOrganization = organizationResponsibleRepository
                .findAllByOrganization_Id(offer.getTender().getOrganization().getId())
                .stream()
                .map(OrganizationResponsible::getEmployee)
                .map(Employee::getId).toList();
        if (!allResponsibleForOrganization.contains(user.getId())) {
            throw new ForbiddenException("You are not allowed to give feedback");
        }
        Review review = Review.builder()
                .reviewee(user)
                .description(feedback)
                .build();
        reviewRepository.saveAndFlush(review);
        return offerMapper.makeOfferResponse(offer);
    }

    public OfferResponse rollbackBid(String bidId, Integer version, String username) {
        if (version < 1) {
            throw new BadRequestException("version must be >= 1");
        }
        Offer offer = offerRepository.findById(UUID.fromString(bidId)).orElseThrow(() ->
                new NotFoundException(String.format("Offer with id %s not found", bidId)));
        Employee user = ValidateEmployee(username,
                new UnauthorizedException(String.format("Username %s not found", username)));
        if (!organizationResponsibleRepository
                .findAllByOrganization_Id(offer.getOrganization().getId())
                .stream()
                .map(OrganizationResponsible::getEmployee)
                .map(Employee::getId).toList().contains(user.getId()) &&
                !offer.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not allowed to rollback bid");
        }
        List<OfferVersion> allVersions = offer.getVersions();
        boolean contains = false;
        for (OfferVersion tenderV : allVersions) {
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
        offer.setVersions(allVersions);
        return offerMapper.makeOfferResponse(offerRepository.saveAndFlush(offer));
    }

    public List<ReviewResponse> getReviews(String tenderId, String authorUsername, String
            requesterUsername, Integer limit, Integer offset) {
        ValidateLimitOffset(limit, offset);
        Tender tender = ValidateTender(tenderId,
                new NotFoundException(String.format("Tender with id %s not found", tenderId)));
        //проверить есть ли requesterUser в ответственных за организацию для тендера
        // вывести отзывы по authorUsername
        Employee authorUser = ValidateEmployee(authorUsername,
                new UnauthorizedException(String.format("Username %s not found", authorUsername)));
        Employee requesterUser = ValidateEmployee(requesterUsername,
                new UnauthorizedException(String.format("Username %s not found", requesterUsername)));
        List<UUID> allResponsibleForOrganization = organizationResponsibleRepository
                .findAllByOrganization_Id(tender.getOrganization().getId())
                .stream()
                .map(OrganizationResponsible::getEmployee)
                .map(Employee::getId).toList();
        if (!allResponsibleForOrganization.contains(requesterUser.getId())) {
            throw new ForbiddenException("You are not allowed to watch reviews");
        }
        List<Review> reviews = reviewRepository.findByReviewee_Id(authorUser.getId(), limit, offset);
        if (reviews.isEmpty()) {
            throw new NotFoundException(String.format("Review with id %s not found", authorUser.getId()));
        }
        return reviews.stream().map(reviewMapper::toReviewResponse).toList();
    }

    private OfferStatus ValidateOfferStatus(String status) {
        try {
            return OfferStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }
    private DecisionType ValidateDecisionType(String decision) {
        try {
            return DecisionType.valueOf(decision);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + decision);
        }
    }

    private Employee ValidateEmployee(String username, RuntimeException e) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> e);
    }

    private Organization ValidateOrganization(String organizationId, RuntimeException e) {
        return organizationRepository.findById(
                UUID.fromString(organizationId)).orElseThrow(() -> e);
    }

    private Tender ValidateTender(String tenderId, RuntimeException e) {
        return tenderRepository.findById(UUID.fromString(tenderId))
                .orElseThrow(() -> e);
    }

    private void ValidateLimitOffset(Integer limit, Integer offset) {
        if (limit > 50 || limit < 0) {
            throw new BadRequestException("limit must be in range [0,50]");
        }
        if (offset < 0) {
            throw new BadRequestException("offset must be >= 0");
        }
    }
}
