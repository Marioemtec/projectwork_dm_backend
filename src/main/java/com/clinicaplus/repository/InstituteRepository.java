package com.clinicaplus.repository;

import com.clinicaplus.model.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    Optional<Institute> findFirstByActiveTrueOrderByIdAsc();
    Optional<Institute> findByName(String name);
}
