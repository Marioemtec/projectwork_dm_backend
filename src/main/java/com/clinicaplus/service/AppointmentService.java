package com.clinicaplus.service;

import com.clinicaplus.dto.AppointmentDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Appointment;
import com.clinicaplus.model.AppointmentStatus;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.model.Patient;
import com.clinicaplus.model.MedicalService;
import com.clinicaplus.repository.AppointmentRepository;
import com.clinicaplus.repository.DoctorRepository;
import com.clinicaplus.repository.PatientRepository;
import com.clinicaplus.repository.MedicalServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppointmentService {

        private static final LocalTime WORKDAY_START = LocalTime.of(9, 0);
        private static final LocalTime WORKDAY_END = LocalTime.of(18, 0);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public AppointmentDTO createAppointment(Long patientId, Long doctorId, Long serviceId, LocalDateTime appointmentDateTime, Integer durationMinutes) {
        Patient patient = patientRepository.findByUserId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with user id: " + patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        MedicalService service = null;
        if (serviceId != null) {
            service = medicalServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        }

        Integer effectiveDuration = service != null ? service.getDurationMinutes() : durationMinutes;
        validateAppointmentConstraints(doctor.getId(), appointmentDateTime, effectiveDuration, null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .service(service)
                .appointmentDateTime(appointmentDateTime)
                .durationMinutes(effectiveDuration)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(savedAppointment);
    }

    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        return mapToDTO(appointment);
    }

    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientUserId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with user id: " + patientUserId));
        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorUserId) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with user id: " + doctorUserId));
        return appointmentRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AppointmentDTO updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(updatedAppointment);
    }

    public AppointmentDTO updateAppointment(Long appointmentId, Long doctorId, LocalDateTime appointmentDateTime, Integer durationMinutes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        validateAppointmentConstraints(doctor.getId(), appointmentDateTime, durationMinutes, appointmentId);

        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(appointmentDateTime);
        appointment.setDurationMinutes(durationMinutes);
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToDTO(updatedAppointment);
    }

    public void deleteAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + appointmentId);
        }
        appointmentRepository.deleteById(appointmentId);
    }

        private void validateAppointmentConstraints(Long doctorId, LocalDateTime startDateTime, Integer durationMinutes, Long appointmentIdToExclude) {
                if (startDateTime == null) {
                        throw new IllegalArgumentException("Appointment date/time is required.");
                }

                if (durationMinutes == null || durationMinutes <= 0) {
                        throw new IllegalArgumentException("Appointment duration must be greater than 0 minutes.");
                }

                DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                        throw new IllegalArgumentException("Appointments can only be booked from Monday to Friday.");
                }

                LocalTime startTime = startDateTime.toLocalTime();
                LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
                LocalTime endTime = endDateTime.toLocalTime();

                if (startTime.isBefore(WORKDAY_START) || endTime.isAfter(WORKDAY_END) || !startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
                        throw new IllegalArgumentException("Appointments must be within business hours: 09:00 - 18:00.");
                }

                LocalDateTime dayStart = startDateTime.toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);

                List<Appointment> sameDayAppointments = appointmentRepository
                                .findByDoctorIdAndAppointmentDateTimeBetweenAndStatusNot(
                                                doctorId,
                                                dayStart,
                                                dayEnd,
                                                AppointmentStatus.CANCELLED
                                );

                boolean overlaps = sameDayAppointments.stream()
                                .filter(existing -> appointmentIdToExclude == null || !existing.getId().equals(appointmentIdToExclude))
                                .anyMatch(existing -> {
                                        LocalDateTime existingStart = existing.getAppointmentDateTime();
                                        LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
                                        return startDateTime.isBefore(existingEnd) && endDateTime.isAfter(existingStart);
                                });

                if (overlaps) {
                        throw new IllegalArgumentException("Selected slot is not available for this doctor based on service duration.");
                }
        }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        AppointmentDTO.AppointmentDTOBuilder builder = AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getFirstName() + " " + 
                           appointment.getPatient().getUser().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFirstName() + " " + 
                          appointment.getDoctor().getUser().getLastName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .durationMinutes(appointment.getDurationMinutes())
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt());
        
        if (appointment.getService() != null) {
            builder.serviceId(appointment.getService().getId())
                   .serviceName(appointment.getService().getName())
                   .servicePrice(appointment.getService().getPrice());
        }
        
        return builder.build();
    }
}
