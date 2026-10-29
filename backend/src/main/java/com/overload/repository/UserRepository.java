package com.overload.repository;

import com.overload.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;

public interface UserRepository extends JpaRepository<User, UUID> {}
