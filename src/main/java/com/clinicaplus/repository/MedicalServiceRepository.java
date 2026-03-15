package com.clinicaplus.repository;

import com.clinicaplus.model.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
    List<MedicalService> findByDoctorId(Long doctorId);
    List<MedicalService> findByDoctorIdAndActiveTrue(Long doctorId);
}
