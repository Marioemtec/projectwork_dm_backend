package com.clinicaplus.service;

import com.clinicaplus.dto.DoctorDTO;
import com.clinicaplus.dto.DoctorStatsDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Appointment;
import com.clinicaplus.model.AppointmentStatus;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.repository.AppointmentRepository;
import com.clinicaplus.repository.DoctorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DoctorDTO getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return mapToDTO(doctor);
    }

    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DoctorStatsDTO getDoctorStats(Long doctorIdOrUserId) {
        // Find doctor by userId or doctorId
        Doctor doctor = doctorRepository.findByUserId(doctorIdOrUserId)
                .orElseGet(() -> doctorRepository.findById(doctorIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Doctor not found with id or userId: " + doctorIdOrUserId)));
        
        // Get all appointments for this doctor
        List<Appointment> allAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        
        // Count unique patients (completed appointments only)
        Set<Long> uniquePatients = new HashSet<>();
        BigDecimal totalEarnings = BigDecimal.ZERO;
        long completedCount = 0;
        
        for (Appointment apt : allAppointments) {
            if (apt.getStatus() == AppointmentStatus.COMPLETED) {
                uniquePatients.add(apt.getPatient().getId());
                completedCount++;
                
                // Add service price to earnings
                if (apt.getService() != null && apt.getService().getPrice() != null) {
                    totalEarnings = totalEarnings.add(apt.getService().getPrice());
                }
            }
        }
        
        return DoctorStatsDTO.builder()
                .totalPatients((long) uniquePatients.size())
                .totalEarnings(totalEarnings)
                .completedAppointments(completedCount)
                .build();
    }

    private DoctorDTO mapToDTO(Doctor doctor) {
        return DoctorDTO.builder()
                .id(doctor.getId())
                .firstName(doctor.getUser().getFirstName())
                .lastName(doctor.getUser().getLastName())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .biography(doctor.getBiography())
                .available(doctor.getAvailable())
                .servicePrice(doctor.getServicePrice())
                .build();
    }
}
