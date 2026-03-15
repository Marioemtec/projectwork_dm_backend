package com.clinicaplus.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private String specialization;
    
    @Column(unique = true)
    private String licenseNumber;
    
    @Column(columnDefinition = "TEXT")
    private String biography;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;
    
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal servicePrice = BigDecimal.ZERO;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
