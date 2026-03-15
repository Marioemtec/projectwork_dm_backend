package com.clinicaplus.controller;

import com.clinicaplus.dto.PatientStatsDTO;
import com.clinicaplus.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
@AllArgsConstructor
@Tag(name = "Patients", description = "Patient management endpoints")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{patientId}/stats")
    @Operation(summary = "Get patient statistics (total to pay and total paid)")
    public ResponseEntity<PatientStatsDTO> getPatientStats(@PathVariable Long patientId) {
        PatientStatsDTO stats = patientService.getPatientStats(patientId);
        return ResponseEntity.ok(stats);
    }
}
