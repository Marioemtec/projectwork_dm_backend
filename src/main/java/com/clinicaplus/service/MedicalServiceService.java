package com.clinicaplus.service;

import com.clinicaplus.dto.MedicalServiceDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.model.MedicalService;
import com.clinicaplus.repository.DoctorRepository;
import com.clinicaplus.repository.MedicalServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;
    private final DoctorRepository doctorRepository;

    public MedicalServiceDTO createService(Long doctorUserId, MedicalServiceDTO serviceDTO) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with userId: " + doctorUserId));

        MedicalService service = MedicalService.builder()
                .doctor(doctor)
                .name(serviceDTO.getName())
                .description(serviceDTO.getDescription())
                .price(serviceDTO.getPrice())
                .durationMinutes(serviceDTO.getDurationMinutes())
                .active(true)
                .build();

        MedicalService savedService = medicalServiceRepository.save(service);
        return mapToDTO(savedService);
    }

    public List<MedicalServiceDTO> getServicesByDoctor(Long doctorIdOrUserId) {
        Doctor doctor = doctorRepository.findById(doctorIdOrUserId)
                .orElseGet(() -> doctorRepository.findByUserId(doctorIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Doctor not found with id or userId: " + doctorIdOrUserId)));

        return medicalServiceRepository.findByDoctorIdAndActiveTrue(doctor.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public MedicalServiceDTO getServiceById(Long serviceId) {
        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        return mapToDTO(service);
    }

    public MedicalServiceDTO updateService(Long serviceId, MedicalServiceDTO serviceDTO) {
        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
                if (serviceDTO.getActive() != null) {
                        service.setActive(serviceDTO.getActive());
                }

        MedicalService updatedService = medicalServiceRepository.save(service);
        return mapToDTO(updatedService);
    }

    public void deleteService(Long serviceId) {
        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        service.setActive(false);
        medicalServiceRepository.save(service);
    }

    private MedicalServiceDTO mapToDTO(MedicalService service) {
        return MedicalServiceDTO.builder()
                .id(service.getId())
                .doctorId(service.getDoctor().getId())
                .doctorName(service.getDoctor().getUser().getFirstName() + " " + service.getDoctor().getUser().getLastName())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .active(service.getActive())
                .build();
    }
}
