package com.overload.service;

import com.overload.domain.*;
import com.overload.dto.Dtos.MovementDto;
import com.overload.dto.Dtos.WeightSystemDto;
import com.overload.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class TaxonomyService {
    private final ExerciseVariantRepository variants;
    private final ExerciseMuscleRepository muscles;

    public TaxonomyService(ExerciseVariantRepository variants, ExerciseMuscleRepository muscles) {
        this.variants = variants; this.muscles = muscles;
    }

    @Transactional(readOnly = true)
    public List<MovementDto> listMovements() {
        Map<Long, Map<String, Double>> byVariant = new HashMap<>();
        for (ExerciseMuscle em : muscles.findAllWithMuscle()) {
            byVariant.computeIfAbsent(em.getId().getVariantId(), k -> new LinkedHashMap<>())
                    .put(em.getMuscle().getName(), em.getWeightPercentage());
        }
        Map<Long, Exercise> exercisesById = new LinkedHashMap<>();
        Map<Long, List<WeightSystemDto>> systemsByExercise = new LinkedHashMap<>();
        for (ExerciseVariant variant : variants.findAllWithTaxonomy()) {
            Exercise exercise = variant.getExercise();
            exercisesById.putIfAbsent(exercise.getId(), exercise);
            systemsByExercise.computeIfAbsent(exercise.getId(), k -> new ArrayList<>())
                    .add(new WeightSystemDto(
                            variant.getId(), variant.getVariantName(), variant.getEquipmentType(),
                            byVariant.getOrDefault(variant.getId(), Map.of())));
        }
        return exercisesById.entrySet().stream().map(entry -> {
            Exercise exercise = entry.getValue();
            return new MovementDto(
                    entry.getKey(), exercise.getBaseName(), exercise.getMuscleGroup().getName(),
                    systemsByExercise.get(entry.getKey()));
        }).toList();
    }
}
