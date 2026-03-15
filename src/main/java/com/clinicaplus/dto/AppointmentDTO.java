package com.clinicaplus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
