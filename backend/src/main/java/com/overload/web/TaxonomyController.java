package com.overload.web;

import com.overload.dto.Dtos.MovementDto;
import com.overload.service.TaxonomyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@Tag(name = "Exercise taxonomy")
public class TaxonomyController {
    private final TaxonomyService service;
    public TaxonomyController(TaxonomyService service) { this.service = service; }

    @GetMapping public List<MovementDto> movements() { return service.listMovements(); }
}
