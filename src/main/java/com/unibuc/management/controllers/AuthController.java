package com.unibuc.management.controllers;

import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.User;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.security.Role;
import com.unibuc.management.services.DoctorService;
import com.unibuc.management.services.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MedicalServiceRepository medicalServiceRepository;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PatientService patientService,
                          DoctorService doctorService,
                          MedicalServiceRepository medicalServiceRepository,
                          HttpSessionSecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.medicalServiceRepository = medicalServiceRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("role", user.getRole());

        if (user.getRole() == Role.PATIENT) {
            Optional<Patient> patientOpt = patientService.getPatientByUsername(username);
            
            if (patientOpt.isEmpty()) {
                patientOpt = patientService.getAllPatients().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(username))
                        .findFirst();
                
                if (patientOpt.isPresent()) {
                    Patient p = patientOpt.get();
                    if (p.getUser() == null) {
                        p.setUser(user);
                        patientService.updatePatient(p.getId(), p);
                    }
                }
            }
            
            patientOpt.ifPresent(p -> userData.put("patientId", p.getId()));
        } else if (user.getRole() == Role.DOCTOR) {
            Optional<Doctor> doctorOpt = doctorService.getDoctorByUsername(username);
            
            // Fallback for users registered before the link was implemented
            if (doctorOpt.isEmpty()) {
                doctorOpt = doctorService.getAllDoctors().stream()
                        .filter(d -> d.getName().equalsIgnoreCase(username) || d.getName().equalsIgnoreCase("Dr. " + username))
                        .findFirst();
                
                // Link them now for future requests
                if (doctorOpt.isPresent()) {
                    Doctor d = doctorOpt.get();
                    if (d.getUser() == null) {
                        d.setUser(user);
                        doctorService.updateDoctor(d.getId(), d);
                    }
                }
            }
            
            doctorOpt.ifPresent(d -> userData.put("doctorId", d.getId()));
        }

        return ResponseEntity.ok(userData);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = Role.valueOf(request.getRole().toUpperCase());
        user.setRole(role);

        User savedUser = userRepository.save(user);

        try {
            if (role == Role.PATIENT) {
                // Validate age for patients - must be at least 18 years old
                if (request.getAge() != null) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.Period period = java.time.Period.between(request.getAge(), today);
                    if (period.getYears() < 18) {
                        userRepository.delete(savedUser);
                        return ResponseEntity.badRequest().body("Pacienții trebuie să aibă minim 18 ani pentru a se înregistra.");
                    }
                }
                
                Patient patient = new Patient();
                String name = request.getFullName();
                patient.setName(name != null && !name.trim().isEmpty() ? name : request.getUsername());
                patient.setAge(request.getAge() != null ? request.getAge() : java.time.LocalDate.now().minusYears(20));
                patient.setSex(request.getSex() != null ? request.getSex() : true);
                patient.setSubscription(false);
                patient.setUser(savedUser);
                patientService.createPatient(patient);
            } else if (role == Role.DOCTOR) {
                Doctor doctor = new Doctor();
                String name = request.getFullName();
                doctor.setName(name != null && !name.trim().isEmpty() ? name : "Dr. " + request.getUsername());
                doctor.setOffice("Cabinet 101");
                doctor.setNumberOfPtodays(21);
                
                List<MedicalService> services = medicalServiceRepository.findAll();
                if (!services.isEmpty()) {
                    doctor.setMedicalService(services.get(0));
                }
                
                doctor.setUser(savedUser);
                doctorService.saveDoctor(doctor);
            }
        } catch (Exception e) {
            System.err.println("Error creating profile for user: " + e.getMessage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account created successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            
            httpRequest.getSession(true);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logout successful");
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> delete(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}