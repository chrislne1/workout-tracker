package com.overload.web;

import com.overload.dto.Dtos.*;
import com.overload.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Workout sessions")
public class SessionController {
    private final WorkoutService service;
    public SessionController(WorkoutService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a workout session")
    public SessionResponse create(@Valid @RequestBody CreateSessionRequest req) { return service.createSession(req); }

    @GetMapping @Operation(summary = "List the 30 most recent sessions")
    public List<SessionResponse> list() { return service.listSessions(); }

    @GetMapping("/{id}")
    public SessionResponse get(@PathVariable UUID id) { return service.getSession(id); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete a session")
    public void delete(@PathVariable UUID id) { service.deleteSession(id); }

    @PostMapping("/{id}/sets") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log a set; returns 1RM, deltas vs last session and any PRs")
    public LogSetResponse logSet(@PathVariable UUID id, @Valid @RequestBody LogSetRequest req) { return service.logSet(id, req); }

    @GetMapping("/{id}/regression")
    @Operation(summary = "Is today's volume for this exercise >10% below the 30-day moving average?")
    public RegressionCheck regression(@PathVariable UUID id, @RequestParam Long variantId) { return service.checkRegression(id, variantId); }

    @PostMapping("/{id}/feedback")
    @Operation(summary = "Record why performance regressed. PURPOSEFUL_DELOAD / INJURY_DISCOMFORT sessions are excluded from the trendline")
    public SessionResponse feedback(@PathVariable UUID id, @Valid @RequestBody FeedbackRequest req) { return service.saveFeedback(id, req.reason()); }
}
