package com.overload.service;

import com.overload.domain.*;
import com.overload.dto.Dtos.*;
import com.overload.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AnalyticsServiceTest {
    WorkoutSetRepository sets = Mockito.mock(WorkoutSetRepository.class);
    WorkoutSessionRepository sessions = Mockito.mock(WorkoutSessionRepository.class);
    MuscleGroupRepository groups = Mockito.mock(MuscleGroupRepository.class);
    SessionFeedbackRepository feedback = Mockito.mock(SessionFeedbackRepository.class);
    UUID user = UUID.randomUUID();
    AnalyticsService service = new AnalyticsService(sets, sessions, groups, feedback, user);

    private MuscleGroup mg(long id, String name, int optimal) {
        MuscleGroup m = new MuscleGroup(); m.setId(id); m.setName(name); m.setOptimalSetsPerWeek(optimal); return m;
    }

    @Test void computesPushPullAndAntagonistRatios() {
        when(groups.findAll()).thenReturn(List.of(mg(1, "Chest", 12), mg(2, "Back", 14), mg(4, "Shoulders", 10), mg(3, "Legs", 12)));
        when(sets.muscleVolume(eq(user), any())).thenReturn(List.of(
                new Object[]{1L, "Chest", 12, 7000.0, 12.0},
                new Object[]{4L, "Shoulders", 10, 3000.0, 8.0},
                new Object[]{2L, "Back", 14, 5000.0, 14.0}));
        VolumeDistribution d = service.volumeDistribution(AnalyticsWindow.D30);
        assertEquals(10000.0, d.pushVolume());
        assertEquals(5000.0, d.pullVolume());
        assertEquals(2.0, d.pushToPullRatio());
        assertEquals(1.4, d.pairs().get(0).ratio());
        MuscleVolume legs = d.muscles().stream().filter(m -> m.muscle().equals("Legs")).findFirst().orElseThrow();
        assertEquals(0.0, legs.volume());
        MuscleVolume chest = d.muscles().stream().filter(m -> m.muscle().equals("Chest")).findFirst().orElseThrow();
        assertEquals(12 / (30 / 7.0), chest.weeklySets(), 0.01);
    }
    @Test void noPullVolumeYieldsNullRatio() {
        when(groups.findAll()).thenReturn(List.of(mg(1, "Chest", 12), mg(2, "Back", 14)));
        when(sets.muscleVolume(eq(user), any())).thenReturn(List.<Object[]>of(new Object[]{1L, "Chest", 12, 100.0, 1.0}));
        assertNull(service.volumeDistribution(AnalyticsWindow.D90).pushToPullRatio());
    }
    @Test void deloadAndInjurySessionsAreExcludedFromTrend() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID(), d = UUID.randomUUID();
        LocalDate day0 = LocalDate.of(2026, 9, 1);
        when(sets.sessionBestE1rm(user, 1L)).thenReturn(List.of(
                new Object[]{a, day0, 100.0}, new Object[]{b, day0.plusDays(7), 105.0},
                new Object[]{c, day0.plusDays(14), 60.0},   // deload
                new Object[]{d, day0.plusDays(21), 115.0}));
        when(feedback.findAllById(any())).thenReturn(List.of(new SessionFeedback(c, RegressionReason.PURPOSEFUL_DELOAD)));
        ProgressTrend t = service.progress(1L);
        assertTrue(t.points().get(2).excluded());
        assertEquals(5.0, t.velocityPerWeek(), 0.01); // slope from 100,105,115 w/o the 60 -> ~5.14
    }
    @Test void fatigueSessionStaysInTrend() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        when(sets.sessionBestE1rm(user, 1L)).thenReturn(List.of(
                new Object[]{a, LocalDate.of(2026, 9, 1), 100.0}, new Object[]{b, LocalDate.of(2026, 9, 8), 90.0}));
        when(feedback.findAllById(any())).thenReturn(List.of(new SessionFeedback(b, RegressionReason.FATIGUE_POOR_SLEEP)));
        ProgressTrend t = service.progress(1L);
        assertFalse(t.points().get(1).excluded());
        assertEquals(-10.0, t.velocityPerWeek());
    }
}
