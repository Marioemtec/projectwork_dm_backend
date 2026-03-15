package com.clinicaplus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalReportDTO {
    private Long id;
    private Long appointmentId;
    private String doctorName;
    private String title;
    private String diagnosis;
    private String prescription;
    private String notes;
    private LocalDateTime createdAt;
}
