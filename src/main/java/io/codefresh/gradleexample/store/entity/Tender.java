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
@Table(name = "tender")
public class Tender {
    @Id
    @GeneratedValue
    @Column(name = "tender_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TenderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(referencedColumnName = "tender_id")
    private List<TenderVersion> versions;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Employee creator;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


}
