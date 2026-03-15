package com.clinicaplus.controller;

import com.clinicaplus.dto.DoctorDTO;
import com.clinicaplus.dto.DoctorStatsDTO;
import com.clinicaplus.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@AllArgsConstructor
@Tag(name = "Doctors", description = "Doctor management endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Get all doctors")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long doctorId) {
        DoctorDTO doctor = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get doctors by specialization")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<DoctorDTO> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available doctors")
    public ResponseEntity<List<DoctorDTO>> getAvailableDoctors() {
        List<DoctorDTO> doctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{doctorId}/stats")
    @Operation(summary = "Get doctor statistics (total patients and earnings)")
    public ResponseEntity<DoctorStatsDTO> getDoctorStats(@PathVariable Long doctorId) {
        DoctorStatsDTO stats = doctorService.getDoctorStats(doctorId);
        return ResponseEntity.ok(stats);
    }
}
