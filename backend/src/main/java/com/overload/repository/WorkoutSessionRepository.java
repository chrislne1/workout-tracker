package com.overload.repository;

import com.overload.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    Optional<WorkoutSession> findByIdAndDeletedAtIsNull(UUID id);
    List<WorkoutSession> findByUserIdAndDeletedAtIsNullOrderBySessionDateDesc(UUID userId, Pageable pageable);

    @Query("select min(s.sessionDate) from WorkoutSession s where s.user.id = :userId and s.deletedAt is null")
    Optional<LocalDate> findEarliestSessionDate(@Param("userId") UUID userId);
}
