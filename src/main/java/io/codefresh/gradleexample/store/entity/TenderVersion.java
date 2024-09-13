package io.codefresh.gradleexample.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenderVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    private Integer version;
    private boolean active;
}
