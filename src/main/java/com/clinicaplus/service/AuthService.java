package com.clinicaplus.service;

import com.clinicaplus.dto.AuthResponse;
import com.clinicaplus.dto.LoginRequest;
import com.clinicaplus.dto.RegisterRequest;
import com.clinicaplus.dto.UserDTO;
import com.clinicaplus.exception.DuplicateResourceException;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.model.Patient;
import com.clinicaplus.model.User;
import com.clinicaplus.model.UserRole;
import com.clinicaplus.repository.DoctorRepository;
import com.clinicaplus.repository.PatientRepository;
import com.clinicaplus.repository.UserRepository;
import com.clinicaplus.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Create user
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.valueOf(request.getRole().toUpperCase()))
                .active(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // Create patient or doctor based on role
        if (UserRole.PATIENT.name().equals(request.getRole().toUpperCase())) {
            Patient patient = Patient.builder()
                    .user(savedUser)
                    .taxCode(request.getTaxCode())
                    .build();
            patientRepository.save(patient);
        } else if (UserRole.DOCTOR.name().equals(request.getRole().toUpperCase())) {
            Doctor doctor = Doctor.builder()
                    .user(savedUser)
                    .specialization(request.getSpecialization())
                    .available(true)
                    .build();
            doctorRepository.save(doctor);
        }

        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        UserDTO userDTO = mapToUserDTO(savedUser);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(token)
                .user(userDTO)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.clinicaplus.exception.ResourceNotFoundException(
                        "Utente non trovato: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (!user.getActive()) {
            throw new IllegalArgumentException("User account is not active");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        UserDTO userDTO = mapToUserDTO(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(token)
                .user(userDTO)
                .message("User logged in successfully")
                .build();
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
