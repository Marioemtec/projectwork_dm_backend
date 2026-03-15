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
public class DoctorStatsDTO {
    private Long totalPatients;
    private BigDecimal totalEarnings;
    private Long completedAppointments;
}
