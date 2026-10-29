package com.overload.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}
