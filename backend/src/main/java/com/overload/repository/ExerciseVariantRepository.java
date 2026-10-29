package com.overload.repository;

import com.overload.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;

public interface ExerciseVariantRepository extends JpaRepository<ExerciseVariant, Long> {
    @Query("select v from ExerciseVariant v join fetch v.exercise e join fetch e.muscleGroup order by e.baseName, v.equipmentType")
    List<ExerciseVariant> findAllWithTaxonomy();
}
