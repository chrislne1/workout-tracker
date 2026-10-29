package com.overload.web;

import com.overload.domain.*;
import com.overload.dto.Dtos.*;
import com.overload.exception.ResourceNotFoundException;
import com.overload.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {
    @Autowired MockMvc mvc;
    @MockBean WorkoutService service;
    final UUID id = UUID.randomUUID();

    @Test void regressionEndpointReturnsTriggerPayload() throws Exception {
        when(service.checkRegression(id, 1L)).thenReturn(new RegressionCheck(true, 800, 1000, 20,
                List.of(RegressionReason.values())));
        mvc.perform(get("/api/sessions/{id}/regression", id).param("variantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regression").value(true))
                .andExpect(jsonPath("$.dropPercent").value(20.0))
                .andExpect(jsonPath("$.reasons.length()").value(4));
    }
    @Test void regressionEndpointStaysQuietWithinThreshold() throws Exception {
        when(service.checkRegression(id, 1L)).thenReturn(new RegressionCheck(false, 950, 1000, 5, List.of()));
        mvc.perform(get("/api/sessions/{id}/regression", id).param("variantId", "1"))
                .andExpect(jsonPath("$.regression").value(false));
    }
    @Test void logSetReturnsPrAchievements() throws Exception {
        SetDto dto = new SetDto(UUID.randomUUID(), 1L, "Incline Dumbbell Press", 1, 80, 8, SetType.WORKING, false, 0, 101.33);
        when(service.logSet(eq(id), any())).thenReturn(new LogSetResponse(dto, new SetDelta(true, 5, 1, 12.5), List.of("ESTIMATED_1RM_PR")));
        mvc.perform(post("/api/sessions/{id}/sets", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":1,\"setType\":\"WORKING\",\"weight\":80,\"reps\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.achievements[0]").value("ESTIMATED_1RM_PR"))
                .andExpect(jsonPath("$.delta.weightDelta").value(5.0));
    }
    @Test void negativeWeightReturnsProblemDetail() throws Exception {
        mvc.perform(post("/api/sessions/{id}/sets", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":1,\"setType\":\"WORKING\",\"weight\":-5,\"reps\":8}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.weight").exists());
    }
    @Test void zeroRepsRejected() throws Exception {
        mvc.perform(post("/api/sessions/{id}/sets", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":1,\"setType\":\"WORKING\",\"weight\":50,\"reps\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.reps").exists());
    }
    @Test void unknownSessionReturns404() throws Exception {
        when(service.getSession(id)).thenThrow(new ResourceNotFoundException("Workout session not found"));
        mvc.perform(get("/api/sessions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }
    @Test void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }
    @Test void unexpectedErrorsAreSanitised() throws Exception {
        when(service.listSessions()).thenThrow(new IllegalStateException("db password leaked"));
        mvc.perform(get("/api/sessions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
