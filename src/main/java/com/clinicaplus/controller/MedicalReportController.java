package com.clinicaplus.controller;

import com.clinicaplus.dto.MedicalReportDTO;
import com.clinicaplus.service.MedicalReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@AllArgsConstructor
@Tag(name = "Medical Reports", description = "Medical report management endpoints")
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    @PostMapping
    @Operation(summary = "Create a new medical report")
    public ResponseEntity<MedicalReportDTO> createReport(@RequestBody Map<String, Object> request) {
        Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
        Long doctorId = Long.parseLong(request.get("doctorId").toString());
        String title = request.get("title").toString();
        String diagnosis = request.get("diagnosis").toString();
        String prescription = request.get("prescription") != null ? request.get("prescription").toString() : "";
        String notes = request.get("notes") != null ? request.get("notes").toString() : "";

        MedicalReportDTO report = medicalReportService.createReport(appointmentId, doctorId, title, diagnosis, prescription, notes);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<MedicalReportDTO> getReportById(@PathVariable Long reportId) {
        MedicalReportDTO report = medicalReportService.getReportById(reportId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get reports for an appointment")
    public ResponseEntity<List<MedicalReportDTO>> getReportsByAppointmentId(@PathVariable Long appointmentId) {
        List<MedicalReportDTO> reports = medicalReportService.getReportsByAppointmentId(appointmentId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get reports for a patient")
    public ResponseEntity<List<MedicalReportDTO>> getReportsByPatientId(@PathVariable Long patientId) {
        List<MedicalReportDTO> reports = medicalReportService.getReportsByPatientId(patientId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get reports for a doctor")
    public ResponseEntity<List<MedicalReportDTO>> getReportsByDoctorId(@PathVariable Long doctorId) {
        List<MedicalReportDTO> reports = medicalReportService.getReportsByDoctorId(doctorId);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update a medical report")
    public ResponseEntity<MedicalReportDTO> updateReport(@PathVariable Long reportId, @RequestBody Map<String, Object> request) {
        String title = request.get("title").toString();
        String diagnosis = request.get("diagnosis").toString();
        String prescription = request.get("prescription") != null ? request.get("prescription").toString() : "";
        String notes = request.get("notes") != null ? request.get("notes").toString() : "";

        MedicalReportDTO report = medicalReportService.updateReport(reportId, title, diagnosis, prescription, notes);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a medical report")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        medicalReportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reportId}/download")
    @Operation(summary = "Download a medical report as PDF")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId) {
        byte[] pdfContent = medicalReportService.generateReportPDF(reportId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename("referto_" + reportId + ".pdf", StandardCharsets.UTF_8)
                .build()
        );
        headers.setContentLength(pdfContent.length);
        
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
