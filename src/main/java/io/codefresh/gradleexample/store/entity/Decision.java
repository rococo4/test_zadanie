package io.codefresh.gradleexample.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class Decision {
    @Enumerated(EnumType.STRING)
    private DecisionType decision;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id",referencedColumnName = "id")
    private Employee decisionMaker;

}
