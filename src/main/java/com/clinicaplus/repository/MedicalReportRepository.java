package com.clinicaplus.repository;

import com.clinicaplus.model.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findByAppointmentId(Long appointmentId);
    List<MedicalReport> findByDoctorId(Long doctorId);
    List<MedicalReport> findByAppointmentPatientId(Long patientId);
}
