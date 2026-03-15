package com.clinicaplus.controller;

import com.clinicaplus.dto.MedicalServiceDTO;
import com.clinicaplus.service.MedicalServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@AllArgsConstructor
@Tag(name = "Medical Services", description = "Medical services endpoints")
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    @PostMapping("/doctor/{doctorId}")
    @Operation(summary = "Create a new medical service")
    public ResponseEntity<MedicalServiceDTO> createService(
            @PathVariable Long doctorId,
            @RequestBody MedicalServiceDTO serviceDTO) {
        MedicalServiceDTO createdService = medicalServiceService.createService(doctorId, serviceDTO);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get all services for a doctor")
    public ResponseEntity<List<MedicalServiceDTO>> getServicesByDoctor(@PathVariable Long doctorId) {
        List<MedicalServiceDTO> services = medicalServiceService.getServicesByDoctor(doctorId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Get a service by ID")
    public ResponseEntity<MedicalServiceDTO> getServiceById(@PathVariable Long serviceId) {
        MedicalServiceDTO service = medicalServiceService.getServiceById(serviceId);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/{serviceId}")
    @Operation(summary = "Update a service")
    public ResponseEntity<MedicalServiceDTO> updateService(
            @PathVariable Long serviceId,
            @RequestBody MedicalServiceDTO serviceDTO) {
        MedicalServiceDTO updatedService = medicalServiceService.updateService(serviceId, serviceDTO);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{serviceId}")
    @Operation(summary = "Delete a service")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        medicalServiceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
