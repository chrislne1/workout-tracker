package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "workout_sessions") @Getter @Setter @NoArgsConstructor
public class WorkoutSession {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(name = "session_date", nullable = false) private LocalDate sessionDate;
    @Enumerated(EnumType.STRING) @Column(name = "split_type", nullable = false) private SplitType splitType;
    private String notes;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
}
