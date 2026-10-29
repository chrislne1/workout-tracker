package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "exercises") @Getter @Setter @NoArgsConstructor
public class Exercise {
    @Id private Long id;
    @Column(name = "base_name", nullable = false) private String baseName;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "muscle_group_id") private MuscleGroup muscleGroup;
}
