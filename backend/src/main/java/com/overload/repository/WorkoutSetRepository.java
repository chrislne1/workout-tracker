package com.overload.repository;

import com.overload.domain.WorkoutSet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

/**
 * All analytics queries exclude WARMUP sets and soft-deleted sessions.
 * Bodyweight sets use (bodyweight + added weight) as their load.
 */
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {
    String LOAD = "(case when s.bodyweight = true then s.weight + s.addedWeight else s.weight end)";
    String WORKING = "s.setType <> com.overload.domain.SetType.WARMUP";

    List<WorkoutSet> findBySessionIdOrderByVariantIdAscSetNumberAsc(UUID sessionId);

    long countBySessionIdAndVariantId(UUID sessionId, Long variantId);

    /** Working volume load of one exercise within one session. */
    @Query("select coalesce(sum(" + LOAD + " * s.reps), 0.0) from WorkoutSet s " +
           "where s.session.id = :sessionId and s.variant.id = :variantId and " + WORKING)
    Double sessionVolume(@Param("sessionId") UUID sessionId, @Param("variantId") Long variantId);

    /** Per-session working volume for an exercise inside [from, to] (excluding the current session). Rows: [sessionId, volume]. */
    @Query("select ses.id, sum(" + LOAD + " * s.reps) from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null " +
           "and ses.id <> :excludeSessionId and ses.sessionDate >= :from and ses.sessionDate <= :to and " + WORKING + " group by ses.id")
    List<Object[]> sessionVolumes(@Param("userId") UUID userId, @Param("variantId") Long variantId,
                                  @Param("from") LocalDate from, @Param("to") LocalDate to,
                                  @Param("excludeSessionId") UUID excludeSessionId);

    @Query("select max(" + LOAD + " * (1 + s.reps / 30.0)) from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null and " + WORKING)
    Double bestEstimatedOneRepMax(@Param("userId") UUID userId, @Param("variantId") Long variantId);

    @Query("select max(" + LOAD + ") from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null and " + WORKING)
    Double heaviestLoad(@Param("userId") UUID userId, @Param("variantId") Long variantId);

    @Query("select max(s.reps) from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null " +
           "and " + LOAD + " = :load and " + WORKING)
    Integer bestRepsAtLoad(@Param("userId") UUID userId, @Param("variantId") Long variantId, @Param("load") double load);

    @Query("select s from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null and ses.id <> :sessionId and " + WORKING +
           " order by ses.sessionDate desc, s.setNumber desc")
    List<WorkoutSet> previousWorkingSets(@Param("userId") UUID userId, @Param("variantId") Long variantId,
                                         @Param("sessionId") UUID sessionId, Pageable pageable);

    /** Best e1RM per session for the strength trendline. Rows: [sessionId, sessionDate, bestE1rm]. */
    @Query("select ses.id, ses.sessionDate, max(" + LOAD + " * (1 + s.reps / 30.0)) from WorkoutSet s join s.session ses " +
           "where ses.user.id = :userId and s.variant.id = :variantId and ses.deletedAt is null and " + WORKING +
           " group by ses.id, ses.sessionDate order by ses.sessionDate")
    List<Object[]> sessionBestE1rm(@Param("userId") UUID userId, @Param("variantId") Long variantId);

    /**
     * Sliding-window muscle aggregation using fractional attribution from exercise_muscles.
     * Rows: [muscleId, muscleName, optimalSetsPerWeek, fractionalVolume, fractionalHardSets].
     */
    @Query("select mg.id, mg.name, mg.optimalSetsPerWeek, sum(" + LOAD + " * s.reps * em.weightPercentage), sum(em.weightPercentage) " +
           "from WorkoutSet s join s.session ses join ExerciseMuscle em on em.variant.id = s.variant.id join em.muscle mg " +
           "where ses.user.id = :userId and ses.deletedAt is null and ses.sessionDate >= :since and " + WORKING +
           " group by mg.id, mg.name, mg.optimalSetsPerWeek")
    List<Object[]> muscleVolume(@Param("userId") UUID userId, @Param("since") LocalDate since);
}
