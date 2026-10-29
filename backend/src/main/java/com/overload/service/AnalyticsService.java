package com.overload.service;

import com.overload.domain.*;
import com.overload.dto.Dtos.*;
import com.overload.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    static final Set<String> PUSH = Set.of("Chest", "Shoulders");
    static final Set<String> PULL = Set.of("Back");

    private final WorkoutSetRepository sets;
    private final WorkoutSessionRepository sessions;
    private final MuscleGroupRepository muscleGroups;
    private final SessionFeedbackRepository feedback;
    private final UUID userId;

    public AnalyticsService(WorkoutSetRepository sets, WorkoutSessionRepository sessions, MuscleGroupRepository muscleGroups,
                            SessionFeedbackRepository feedback, @Value("${app.default-user-id}") UUID userId) {
        this.sets = sets; this.sessions = sessions; this.muscleGroups = muscleGroups; this.feedback = feedback; this.userId = userId;
    }

    @Transactional(readOnly = true)
    public VolumeDistribution volumeDistribution(AnalyticsWindow window) {
        LocalDate today = LocalDate.now();
        LocalDate since; double weeks;
        if (window.getDays() == null) {
            since = sessions.findEarliestSessionDate(userId).orElse(today);
            weeks = Math.max(1, ChronoUnit.DAYS.between(since, today) / 7.0);
        } else {
            since = today.minusDays(window.getDays());
            weeks = window.getDays() / 7.0;
        }

        Map<String, double[]> agg = new HashMap<>(); // name -> [volume, hardSets]
        for (Object[] r : sets.muscleVolume(userId, since)) {
            agg.put((String) r[1], new double[]{((Number) r[3]).doubleValue(), ((Number) r[4]).doubleValue()});
        }
        double total = agg.values().stream().mapToDouble(a -> a[0]).sum();

        List<MuscleVolume> muscles = muscleGroups.findAll().stream().map(mg -> {
            double[] a = agg.getOrDefault(mg.getName(), new double[]{0, 0});
            double weekly = a[1] / weeks;
            return new MuscleVolume(mg.getName(), StrengthMath.round2(a[0]), StrengthMath.round2(a[1]),
                    StrengthMath.round2(weekly), mg.getOptimalSetsPerWeek(),
                    StrengthMath.round2(weekly / mg.getOptimalSetsPerWeek() * 100),
                    total > 0 ? StrengthMath.round2(a[0] / total * 100) : 0);
        }).sorted(Comparator.comparingDouble(MuscleVolume::volume).reversed()).toList();

        double push = sum(muscles, PUSH), pull = sum(muscles, PULL);
        return new VolumeDistribution(window, muscles, push, pull,
                pull > 0 ? StrengthMath.round2(push / pull) : null,
                List.of(pair(muscles, "Chest", "Back")));
    }

    @Transactional(readOnly = true)
    public ProgressTrend progress(Long variantId) {
        List<Object[]> rows = sets.sessionBestE1rm(userId, variantId);
        Set<UUID> ids = rows.stream().map(r -> (UUID) r[0]).collect(Collectors.toSet());
        Map<UUID, RegressionReason> reasons = feedback.findAllById(ids).stream()
                .collect(Collectors.toMap(SessionFeedback::getSessionId, SessionFeedback::getRegressionReason));

        List<TrendPoint> points = new ArrayList<>();
        List<double[]> forSlope = new ArrayList<>();
        LocalDate first = rows.isEmpty() ? null : (LocalDate) rows.get(0)[1];
        for (Object[] r : rows) {
            LocalDate date = (LocalDate) r[1];
            double e1rm = StrengthMath.round2(((Number) r[2]).doubleValue());
            RegressionReason reason = reasons.get((UUID) r[0]);
            boolean excluded = reason != null && reason.isExcludedFromTrend();
            points.add(new TrendPoint(date, e1rm, excluded));
            if (!excluded) forSlope.add(new double[]{ChronoUnit.DAYS.between(first, date), e1rm});
        }
        return new ProgressTrend(variantId, points, StrengthMath.slopePerWeek(forSlope));
    }

    private static double sum(List<MuscleVolume> list, Set<String> names) {
        return StrengthMath.round2(list.stream().filter(m -> names.contains(m.muscle())).mapToDouble(MuscleVolume::volume).sum());
    }

    private static AntagonistPair pair(List<MuscleVolume> list, String agonist, String antagonist) {
        double a = sum(list, Set.of(agonist)), b = sum(list, Set.of(antagonist));
        return new AntagonistPair(agonist, antagonist, a, b, b > 0 ? StrengthMath.round2(a / b) : null);
    }
}
