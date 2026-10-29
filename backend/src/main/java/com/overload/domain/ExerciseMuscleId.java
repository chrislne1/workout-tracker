package com.overload.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable @Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ExerciseMuscleId implements Serializable {
    private Long variantId;
    private Long muscleId;
}
