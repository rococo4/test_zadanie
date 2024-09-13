package io.codefresh.gradleexample.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offer")
public class Offer {
    @Id
    @GeneratedValue
    @Column(name = "offer_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OfferStatus offerStatus;

    @ManyToOne
    @JoinColumn(name = "tender_id", referencedColumnName = "tender_id")
    private Tender tender;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(referencedColumnName = "offer_id")
    private List<OfferVersion> versions;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Employee author;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private OfferAuthorType authorType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(referencedColumnName = "offer_id")
    List<Decision> decisions;


}
