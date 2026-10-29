package com.overload.service;

import com.overload.domain.*;
import com.overload.dto.Dtos.MovementDto;
import com.overload.repository.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaxonomyServiceTest {
    @Test
    void groupsEquipmentVariantsUnderTheirMovement() {
        MuscleGroup chest = new MuscleGroup();
        chest.setName("Chest");
        Exercise movement = new Exercise();
        movement.setId(1L);
        movement.setBaseName("Chest Press");
        movement.setMuscleGroup(chest);

        ExerciseVariant dumbbells = new ExerciseVariant();
        dumbbells.setId(10L);
        dumbbells.setExercise(movement);
        dumbbells.setVariantName("Dumbbell Chest Press");
        dumbbells.setEquipmentType(EquipmentType.DUMBBELL);
        ExerciseVariant barbell = new ExerciseVariant();
        barbell.setId(11L);
        barbell.setExercise(movement);
        barbell.setVariantName("Barbell Chest Press");
        barbell.setEquipmentType(EquipmentType.BARBELL);

        ExerciseVariantRepository variants = mock(ExerciseVariantRepository.class);
        ExerciseMuscleRepository muscles = mock(ExerciseMuscleRepository.class);
        when(variants.findAllWithTaxonomy()).thenReturn(List.of(dumbbells, barbell));
        when(muscles.findAllWithMuscle()).thenReturn(List.of());

        List<MovementDto> result = new TaxonomyService(variants, muscles).listMovements();

        assertEquals(1, result.size());
        assertEquals("Chest Press", result.get(0).movementName());
        assertEquals("Chest", result.get(0).muscleGroup());
        assertEquals(List.of(EquipmentType.DUMBBELL, EquipmentType.BARBELL),
                result.get(0).weightSystems().stream().map(system -> system.equipmentType()).toList());
        assertEquals(List.of(10L, 11L),
                result.get(0).weightSystems().stream().map(system -> system.variantId()).toList());
    }
}
