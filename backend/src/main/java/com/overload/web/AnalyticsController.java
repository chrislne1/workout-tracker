package com.overload.web;

import com.overload.domain.AnalyticsWindow;
import com.overload.dto.Dtos.*;
import com.overload.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics")
public class AnalyticsController {
    private final AnalyticsService service;
    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping("/volume")
    @Operation(summary = "Muscle volume, hard sets, push:pull and agonist:antagonist ratios. window = D30 | D90 | M6 | Y1 | AOT")
    public VolumeDistribution volume(@RequestParam(defaultValue = "D30") AnalyticsWindow window) { return service.volumeDistribution(window); }

    @GetMapping("/progress/{variantId}")
    @Operation(summary = "Estimated 1RM trend and weekly progression velocity")
    public ProgressTrend progress(@PathVariable Long variantId) { return service.progress(variantId); }
}
