package com.overload.service;

import com.overload.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StrengthMathTest {
    private WorkoutSet set(double w, int reps, SetType type, boolean bw, double added) {
        WorkoutSet s = new WorkoutSet();
        s.setWeight(w); s.setReps(reps); s.setSetType(type); s.setBodyweight(bw); s.setAddedWeight(added);
        return s;
    }

    @Test void epleyMatchesFormula() {
        assertEquals(116.67, StrengthMath.estimateOneRepMax(100, 5), 0.001);
        assertEquals(135.0, StrengthMath.estimateOneRepMax(90, 15), 0.001);
    }
    @Test void oneRepMaxEdgeCases() {
        assertEquals(200.0, StrengthMath.estimateOneRepMax(200, 0));
        assertEquals(0.0, StrengthMath.estimateOneRepMax(0, 10));
        assertThrows(IllegalArgumentException.class, () -> StrengthMath.estimateOneRepMax(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> StrengthMath.estimateOneRepMax(100, -1));
    }
    @Test void volumeExcludesWarmups() {
        List<WorkoutSet> sets = List.of(set(45, 10, SetType.WARMUP, false, 0), set(100, 10, SetType.WORKING, false, 0),
                set(80, 8, SetType.DROPSET, false, 0), set(100, 5, SetType.FAILURE, false, 0));
        assertEquals(1000 + 640 + 500, StrengthMath.volumeLoad(sets));
    }
    @Test void bodyweightSetsAddExtraLoad() {
        assertEquals(1800.0, StrengthMath.volumeLoad(List.of(set(180, 10, SetType.WORKING, true, 0))));
        assertEquals(2300.0, StrengthMath.volumeLoad(List.of(set(180, 10, SetType.WORKING, true, 50))));
    }
    @Test void volumeRejectsNegatives() {
        assertThrows(IllegalArgumentException.class, () -> StrengthMath.volume(-5, 10));
        assertThrows(IllegalArgumentException.class, () -> StrengthMath.volume(5, -10));
        assertEquals(0.0, StrengthMath.volume(0, 10));
    }
    @Test void percentChange() {
        assertEquals(10.0, StrengthMath.percentChange(1000, 1100));
        assertEquals(-25.0, StrengthMath.percentChange(1000, 750));
        assertEquals(0.0, StrengthMath.percentChange(0, 500));
    }
    @Test void regressionBoundary() {
        assertFalse(StrengthMath.isRegression(900, 1000), "exactly 10% is not MORE than 10%");
        assertTrue(StrengthMath.isRegression(899, 1000));
        assertFalse(StrengthMath.isRegression(1200, 1000));
        assertFalse(StrengthMath.isRegression(0, 0), "no baseline, no regression");
    }
    @Test void slope() {
        assertEquals(7.0, StrengthMath.slopePerWeek(List.of(new double[]{0, 100}, new double[]{7, 107}, new double[]{14, 114})));
        assertNull(StrengthMath.slopePerWeek(List.of(new double[]{0, 100})));
    }
}
