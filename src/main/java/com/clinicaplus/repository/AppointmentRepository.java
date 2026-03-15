package com.clinicaplus.repository;

import com.clinicaplus.model.Appointment;
import com.clinicaplus.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetweenAndStatusNot(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status
    );
}
