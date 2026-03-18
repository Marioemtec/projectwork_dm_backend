package com.clinicaplus.service;

import com.clinicaplus.dto.MedicalServiceDTO;
import com.clinicaplus.dto.MedicalServiceAvailabilityDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.model.Institute;
import com.clinicaplus.model.MedicalService;
import com.clinicaplus.model.MedicalServiceAvailability;
import com.clinicaplus.repository.DoctorRepository;
import com.clinicaplus.repository.InstituteRepository;
import com.clinicaplus.repository.MedicalServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MedicalServiceService {

        private static final Set<String> ALLOWED_LOCATIONS = Set.of(
                        "Sede Centro",
                        "Sede Nord",
                        "Sede Sud"
        );

        private static final LocalTime WORKDAY_START = LocalTime.of(9, 0);
        private static final LocalTime WORKDAY_END = LocalTime.of(18, 0);

    private final MedicalServiceRepository medicalServiceRepository;
    private final DoctorRepository doctorRepository;
        private final InstituteRepository instituteRepository;

    public MedicalServiceDTO createService(Long doctorUserId, MedicalServiceDTO serviceDTO) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with userId: " + doctorUserId));

        validateAvailabilities(serviceDTO.getAvailabilities());
        Institute institute = resolveInstituteForService(serviceDTO);

        MedicalService service = MedicalService.builder()
                .doctor(doctor)
                .institute(institute)
                .name(serviceDTO.getName())
                .description(serviceDTO.getDescription())
                .price(serviceDTO.getPrice())
                .durationMinutes(serviceDTO.getDurationMinutes())
                .active(true)
                .build();

        service.setAvailabilities(mapAvailabilities(serviceDTO.getAvailabilities(), service));

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
        service.setDurationMinutes(serviceDTO.getDurationMinutes());

        if (serviceDTO.getAvailabilities() != null) {
            validateAvailabilities(serviceDTO.getAvailabilities());
            service.getAvailabilities().clear();
            service.getAvailabilities().addAll(mapAvailabilities(serviceDTO.getAvailabilities(), service));
        }

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
                List<MedicalServiceAvailabilityDTO> availabilities = service.getAvailabilities()
                                .stream()
                                .sorted(Comparator
                                                .comparing(MedicalServiceAvailability::getLocation)
                                                .thenComparing(MedicalServiceAvailability::getDayOfWeek)
                                                .thenComparing(MedicalServiceAvailability::getStartTime))
                                .map(this::mapAvailabilityToDTO)
                                .collect(Collectors.toList());

        return MedicalServiceDTO.builder()
                .id(service.getId())
                .doctorId(service.getDoctor().getId())
                .doctorName(service.getDoctor().getUser().getFirstName() + " " + service.getDoctor().getUser().getLastName())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .active(service.getActive())
                                .locations(availabilities.stream().map(MedicalServiceAvailabilityDTO::getLocation).distinct().toList())
                                .availabilities(availabilities)
                .build();
    }

    private Institute resolveInstituteForService(MedicalServiceDTO serviceDTO) {
        String preferredInstituteName = serviceDTO.getAvailabilities() != null && !serviceDTO.getAvailabilities().isEmpty()
                ? serviceDTO.getAvailabilities().get(0).getLocation().trim()
                : "Sede Centro";

        return instituteRepository.findByName(preferredInstituteName)
                .or(() -> instituteRepository.findFirstByActiveTrueOrderByIdAsc())
                .orElseGet(() -> instituteRepository.save(Institute.builder()
                        .name(preferredInstituteName)
                        .city("N/D")
                        .address(preferredInstituteName)
                        .active(true)
                        .description("Sede creata automaticamente per compatibilita con il vincolo institute_id.")
                        .build()));
    }

        private List<MedicalServiceAvailability> mapAvailabilities(List<MedicalServiceAvailabilityDTO> availabilityDTOs, MedicalService service) {
                return availabilityDTOs.stream()
                                .map(availabilityDTO -> MedicalServiceAvailability.builder()
                                                .medicalService(service)
                                                .location(availabilityDTO.getLocation().trim())
                                                .dayOfWeek(availabilityDTO.getDayOfWeek())
                                                .startTime(availabilityDTO.getStartTime())
                                                .endTime(availabilityDTO.getEndTime())
                                                .build())
                                .collect(Collectors.toList());
        }

        private MedicalServiceAvailabilityDTO mapAvailabilityToDTO(MedicalServiceAvailability availability) {
                return MedicalServiceAvailabilityDTO.builder()
                                .location(availability.getLocation())
                                .dayOfWeek(availability.getDayOfWeek())
                                .startTime(availability.getStartTime())
                                .endTime(availability.getEndTime())
                                .build();
        }

        private void validateAvailabilities(List<MedicalServiceAvailabilityDTO> availabilities) {
                if (availabilities == null || availabilities.isEmpty()) {
                        throw new IllegalArgumentException("At least one availability slot is required for a service.");
                }

                Set<String> uniqueLocations = availabilities.stream()
                                .map(MedicalServiceAvailabilityDTO::getLocation)
                                .filter(location -> location != null && !location.isBlank())
                                .map(String::trim)
                                .collect(Collectors.toSet());

                if (uniqueLocations.isEmpty()) {
                        throw new IllegalArgumentException("At least one location is required.");
                }

                if (uniqueLocations.size() > 3) {
                        throw new IllegalArgumentException("A service can have up to 3 locations.");
                }

                if (!ALLOWED_LOCATIONS.containsAll(uniqueLocations)) {
                        throw new IllegalArgumentException("Allowed locations are: " + String.join(", ", ALLOWED_LOCATIONS));
                }

                Set<String> duplicateChecker = new HashSet<>();
                for (MedicalServiceAvailabilityDTO availability : availabilities) {
                        if (availability.getLocation() == null || availability.getLocation().isBlank()) {
                                throw new IllegalArgumentException("Availability location is required.");
                        }

                        if (availability.getDayOfWeek() == null) {
                                throw new IllegalArgumentException("Availability dayOfWeek is required.");
                        }

                        if (!Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
                                        .contains(availability.getDayOfWeek())) {
                                throw new IllegalArgumentException("Only Monday to Friday availability is allowed.");
                        }

                        if (availability.getStartTime() == null || availability.getEndTime() == null) {
                                throw new IllegalArgumentException("Availability startTime and endTime are required.");
                        }

                        if (!availability.getStartTime().isBefore(availability.getEndTime())) {
                                throw new IllegalArgumentException("Availability startTime must be earlier than endTime.");
                        }

                        if (availability.getStartTime().isBefore(WORKDAY_START) || availability.getEndTime().isAfter(WORKDAY_END)) {
                                throw new IllegalArgumentException("Availability hours must be within 09:00 and 18:00.");
                        }

                        String uniqueKey = availability.getLocation().trim() + "|" + availability.getDayOfWeek();
                        if (!duplicateChecker.add(uniqueKey)) {
                                throw new IllegalArgumentException("Only one availability slot per location/day is allowed.");
                        }
                }
        }
}
