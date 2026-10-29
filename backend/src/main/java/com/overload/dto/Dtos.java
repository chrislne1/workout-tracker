package com.overload.dto;

import com.overload.domain.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

/** Request/response contracts for the REST API. */
public final class Dtos {
    private Dtos() {}

    // ---- requests ----
    public record CreateSessionRequest(@NotNull LocalDate sessionDate, @NotNull SplitType splitType, @Size(max = 1000) String notes) {}
    public record LogSetRequest(@NotNull Long variantId, @NotNull SetType setType,
                                @NotNull @PositiveOrZero Double weight, @NotNull @Positive Integer reps,
                                boolean bodyweight, @PositiveOrZero Double addedWeight) {}
    public record FeedbackRequest(@NotNull RegressionReason reason) {}

    // ---- workout logging ----
    public record SetDto(UUID id, Long variantId, String variantName, int setNumber, double weight, int reps,
                         SetType setType, boolean bodyweight, double addedWeight, double estimatedOneRepMax) {}
    public record SessionResponse(UUID id, LocalDate sessionDate, SplitType splitType, String notes,
                                  List<SetDto> sets, double workingVolume, RegressionReason regressionReason) {}
    public record SetDelta(boolean hasPrevious, double weightDelta, int repsDelta, double volumeChangePercent) {}
    /** achievements: ESTIMATED_1RM_PR, WEIGHT_PR, REP_PR */
    public record LogSetResponse(SetDto set, SetDelta delta, List<String> achievements) {}
    public record RegressionCheck(boolean regression, double todayVolume, double baselineVolume,
                                  double dropPercent, List<RegressionReason> reasons) {}

    // ---- taxonomy ----
    public record MovementDto(Long id, String movementName, String muscleGroup,
                              List<WeightSystemDto> weightSystems) {}
    public record WeightSystemDto(Long variantId, String variantName, EquipmentType equipmentType,
                                  Map<String, Double> muscles) {}

    // ---- analytics ----
    public record MuscleVolume(String muscle, double volume, double hardSets, double weeklySets,
                               int optimalSetsPerWeek, double percentOfOptimal, double sharePercent) {}
    public record AntagonistPair(String agonist, String antagonist, double agonistVolume, double antagonistVolume, Double ratio) {}
    public record VolumeDistribution(AnalyticsWindow window, List<MuscleVolume> muscles, double pushVolume,
                                     double pullVolume, Double pushToPullRatio, List<AntagonistPair> pairs) {}
    public record TrendPoint(LocalDate date, double estimatedOneRepMax, boolean excluded) {}
    public record ProgressTrend(Long variantId, List<TrendPoint> points, Double velocityPerWeek) {}
}
