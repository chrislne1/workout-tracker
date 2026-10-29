package com.overload.service;

import com.overload.dto.Dtos.RegressionCheck;
import com.overload.repository.WorkoutSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegressionServiceTest {
    @Mock WorkoutSetRepository sets;
    @InjectMocks RegressionService service;
    final UUID user = UUID.randomUUID(), session = UUID.randomUUID();
    final LocalDate today = LocalDate.of(2026, 10, 29);

    private void history(double... volumes) {
        List<Object[]> rows = new ArrayList<>();
        for (double v : volumes) rows.add(new Object[]{UUID.randomUUID(), v});
        when(sets.sessionVolumes(eq(user), eq(1L), any(), any(), eq(session))).thenReturn(rows);
    }

    @Test void flagsDropOverTenPercent() {
        when(sets.sessionVolume(session, 1L)).thenReturn(800.0);
        history(1000, 1000, 1000);
        RegressionCheck r = service.evaluate(user, session, today, 1L);
        assertTrue(r.regression());
        assertEquals(20.0, r.dropPercent());
        assertEquals(1000.0, r.baselineVolume());
        assertEquals(4, r.reasons().size());
    }
    @Test void ignoresDropUnderTenPercent() {
        when(sets.sessionVolume(session, 1L)).thenReturn(950.0);
        history(1000, 1000);
        assertFalse(service.evaluate(user, session, today, 1L).regression());
    }
    @Test void noHistoryMeansNoRegression() {
        when(sets.sessionVolume(session, 1L)).thenReturn(100.0);
        history();
        RegressionCheck r = service.evaluate(user, session, today, 1L);
        assertFalse(r.regression());
        assertEquals(0.0, r.baselineVolume());
    }
    @Test void usesAverageOfPriorSessions() {
        when(sets.sessionVolume(session, 1L)).thenReturn(1000.0);
        history(1200, 1000, 800, 1400); // avg 1100 -> 9.09% drop
        assertFalse(service.evaluate(user, session, today, 1L).regression());
    }
}
