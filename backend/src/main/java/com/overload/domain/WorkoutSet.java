package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "workout_sets") @Getter @Setter @NoArgsConstructor
public class WorkoutSet {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id") private WorkoutSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "variant_id") private ExerciseVariant variant;
    @Column(name = "set_number", nullable = false) private int setNumber;
    @Column(nullable = false) private Double weight;
    @Column(nullable = false) private Integer reps;
    @Enumerated(EnumType.STRING) @Column(name = "set_type", nullable = false) private SetType setType;
    @Column(name = "is_bodyweight", nullable = false) private boolean bodyweight;
    @Column(name = "added_weight", nullable = false) private Double addedWeight = 0.0;

    /** Load actually moved: bodyweight sets add the extra weight on top. */
    public double effectiveLoad() { return bodyweight ? weight + addedWeight : weight; }
}
