package com.overload.service;

import com.overload.domain.RegressionReason;
import com.overload.dto.Dtos.RegressionCheck;
import com.overload.repository.WorkoutSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

/** Detects a >10% drop of today's working volume vs. the user's 30-day moving average. */
@Service
public class RegressionService {
    private static final int WINDOW_DAYS = 30;
    private final WorkoutSetRepository sets;

    public RegressionService(WorkoutSetRepository sets) { this.sets = sets; }

    @Transactional(readOnly = true)
    public RegressionCheck evaluate(UUID userId, UUID sessionId, LocalDate date, Long variantId) {
        double today = sets.sessionVolume(sessionId, variantId);
        List<Object[]> rows = sets.sessionVolumes(userId, variantId, date.minusDays(WINDOW_DAYS), date, sessionId);
        double baseline = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).average().orElse(0);
        boolean regression = StrengthMath.isRegression(today, baseline);
        double drop = StrengthMath.round2(StrengthMath.dropFraction(today, baseline) * 100);
        return new RegressionCheck(regression, StrengthMath.round2(today), StrengthMath.round2(baseline), drop,
                regression ? List.of(RegressionReason.values()) : List.of());
    }
}
