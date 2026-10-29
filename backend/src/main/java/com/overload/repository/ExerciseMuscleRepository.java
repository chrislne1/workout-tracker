package com.overload.repository;

import com.overload.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;

public interface ExerciseMuscleRepository extends JpaRepository<ExerciseMuscle, ExerciseMuscleId> {
    @Query("select em from ExerciseMuscle em join fetch em.muscle")
    List<ExerciseMuscle> findAllWithMuscle();
}
