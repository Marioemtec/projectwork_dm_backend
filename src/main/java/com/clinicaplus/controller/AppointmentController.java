package com.clinicaplus.controller;

import com.clinicaplus.dto.AppointmentDTO;
import com.clinicaplus.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@AllArgsConstructor
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Create a new appointment")
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody Map<String, Object> request) {
        Long patientId = Long.parseLong(request.get("patientId").toString());
        Long doctorId = Long.parseLong(request.get("doctorId").toString());
        LocalDateTime appointmentDateTime = LocalDateTime.parse(request.get("appointmentDateTime").toString());
        Integer durationMinutes = Integer.parseInt(request.get("durationMinutes").toString());
        String location = request.containsKey("location") && request.get("location") != null
            ? request.get("location").toString()
            : null;
        
        Long serviceId = null;
        if (request.containsKey("serviceId") && request.get("serviceId") != null) {
            serviceId = Long.parseLong(request.get("serviceId").toString());
        }

        AppointmentDTO appointment = appointmentService.createAppointment(patientId, doctorId, serviceId, location, appointmentDateTime, durationMinutes);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get appointments for a patient")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get appointments for a doctor")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{appointmentId}")
    @Operation(summary = "Update an appointment")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long appointmentId, @RequestBody Map<String, Object> request) {
        Long doctorId = Long.parseLong(request.get("doctorId").toString());
        LocalDateTime appointmentDateTime = LocalDateTime.parse(request.get("appointmentDateTime").toString());
        Integer durationMinutes = Integer.parseInt(request.get("durationMinutes").toString());

        AppointmentDTO appointment = appointmentService.updateAppointment(appointmentId, doctorId, appointmentDateTime, durationMinutes);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{appointmentId}/status")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(@PathVariable Long appointmentId, @RequestBody Map<String, String> request) {
        AppointmentDTO appointment = appointmentService.updateAppointmentStatus(appointmentId, request.get("status"));
        return ResponseEntity.ok(appointment);
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Delete an appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
