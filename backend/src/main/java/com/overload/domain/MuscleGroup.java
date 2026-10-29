package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "muscle_groups") @Getter @Setter @NoArgsConstructor
public class MuscleGroup {
    @Id private Long id;
    @Column(nullable = false, unique = true) private String name;
    @Column(name = "optimal_sets_per_week", nullable = false) private int optimalSetsPerWeek;
}
