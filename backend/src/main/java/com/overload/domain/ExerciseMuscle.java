package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "exercise_muscles") @Getter @Setter @NoArgsConstructor
public class ExerciseMuscle {
    @EmbeddedId private ExerciseMuscleId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("variantId") @JoinColumn(name = "variant_id") private ExerciseVariant variant;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("muscleId") @JoinColumn(name = "muscle_id") private MuscleGroup muscle;
    @Column(name = "weight_percentage", nullable = false) private Double weightPercentage;
}
