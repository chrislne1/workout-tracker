package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "exercise_variants") @Getter @Setter @NoArgsConstructor
public class ExerciseVariant {
    @Id private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exercise_id") private Exercise exercise;
    @Column(name = "variant_name", nullable = false) private String variantName;
    @Enumerated(EnumType.STRING) @Column(name = "equipment_type", nullable = false) private EquipmentType equipmentType;
}
