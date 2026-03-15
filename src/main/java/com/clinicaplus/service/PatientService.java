package com.clinicaplus.service;

import com.clinicaplus.dto.PatientStatsDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Appointment;
import com.clinicaplus.model.AppointmentStatus;
import com.clinicaplus.model.Patient;
import com.clinicaplus.repository.AppointmentRepository;
import com.clinicaplus.repository.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientStatsDTO getPatientStats(Long patientIdOrUserId) {
        // Find patient by userId first, then by patientId
        Patient patient = patientRepository.findByUserId(patientIdOrUserId)
                .orElseGet(() -> patientRepository.findById(patientIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Patient not found with id or userId: " + patientIdOrUserId)));
        
        // Get all appointments for this patient
        List<Appointment> allAppointments = appointmentRepository.findByPatientId(patient.getId());
        
        BigDecimal totalToPay = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        long totalCount = 0;
        
        for (Appointment apt : allAppointments) {
            if (apt.getService() != null && apt.getService().getPrice() != null) {
                if (apt.getStatus() == AppointmentStatus.COMPLETED) {
                    // Visita completata = pagata
                    totalPaid = totalPaid.add(apt.getService().getPrice());
                } else if (apt.getStatus() == AppointmentStatus.SCHEDULED || 
                           apt.getStatus() == AppointmentStatus.CONFIRMED) {
                    // Visita prenotata (SCHEDULED) o confermata (CONFIRMED) = da pagare
                    totalToPay = totalToPay.add(apt.getService().getPrice());
                }
                // CANCELLED appointments non vengono contati
                totalCount++;
            }
        }
        
        return PatientStatsDTO.builder()
                .totalToPay(totalToPay)
                .totalPaid(totalPaid)
                .totalAppointments(totalCount)
                .build();
    }
}
