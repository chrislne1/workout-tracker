package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "session_feedback") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SessionFeedback {
    @Id @Column(name = "session_id") private UUID sessionId;
    @Enumerated(EnumType.STRING) @Column(name = "regression_reason", nullable = false) private RegressionReason regressionReason;
}
