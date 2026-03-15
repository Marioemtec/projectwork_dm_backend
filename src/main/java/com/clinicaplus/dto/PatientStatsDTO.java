package com.clinicaplus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientStatsDTO {
    private BigDecimal totalToPay;
    private BigDecimal totalPaid;
    private Long totalAppointments;
}
