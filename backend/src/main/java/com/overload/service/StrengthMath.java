package com.overload.service;

import com.overload.domain.SetType;
import com.overload.domain.WorkoutSet;
import java.util.Collection;

/** Pure, side-effect-free strength math (unit-tested in isolation). */
public final class StrengthMath {
    /** A session must fall MORE than 10% under the baseline to count as a regression. */
    public static final double REGRESSION_THRESHOLD = 0.10;

    private StrengthMath() {}

    /** Epley: 1RM = weight x (1 + reps / 30). */
    public static double estimateOneRepMax(double weight, int reps) {
        if (weight < 0) throw new IllegalArgumentException("Weight must not be negative");
        if (reps < 0) throw new IllegalArgumentException("Reps must not be negative");
        return round2(weight * (1 + reps / 30.0));
    }

    public static double setLoad(double weight, boolean bodyweight, double addedWeight) {
        if (weight < 0 || addedWeight < 0) throw new IllegalArgumentException("Weight must not be negative");
        return bodyweight ? weight + addedWeight : weight;
    }

    public static double volume(double load, int reps) {
        if (load < 0 || reps < 0) throw new IllegalArgumentException("Load and reps must not be negative");
        return load * reps;
    }

    /** Sum of load x reps for working sets. WARMUP sets are always excluded. */
    public static double volumeLoad(Collection<WorkoutSet> sets) {
        return sets.stream()
                .filter(s -> s.getSetType() != SetType.WARMUP)
                .mapToDouble(s -> volume(s.effectiveLoad(), s.getReps()))
                .sum();
    }

    public static double percentChange(double previous, double current) {
        return previous <= 0 ? 0 : round2((current - previous) / previous * 100);
    }

    /** Fraction (0..1) that today is below baseline; 0 when there is no baseline. */
    public static double dropFraction(double today, double baseline) {
        return baseline <= 0 ? 0 : Math.max(0, (baseline - today) / baseline);
    }

    public static boolean isRegression(double today, double baseline) {
        return dropFraction(today, baseline) > REGRESSION_THRESHOLD + 1e-9;
    }

    /** Least-squares slope of value over day-offset, returned per week. Needs >= 2 distinct days. */
    public static Double slopePerWeek(java.util.List<double[]> dayValuePairs) {
        int n = dayValuePairs.size();
        if (n < 2) return null;
        double sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (double[] p : dayValuePairs) { sx += p[0]; sy += p[1]; sxy += p[0] * p[1]; sxx += p[0] * p[0]; }
        double denom = n * sxx - sx * sx;
        if (denom == 0) return null;
        return round2((n * sxy - sx * sy) / denom * 7);
    }

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
