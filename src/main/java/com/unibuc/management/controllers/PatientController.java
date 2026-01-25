package com.unibuc.management.controllers;

import com.unibuc.management.entities.Patient;
import com.unibuc.management.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(java.util.Map.of(
            "authenticated", auth != null && auth.isAuthenticated(),
            "username", auth != null ? auth.getName() : "anonymous",
            "authorities", auth != null ? auth.getAuthorities().toString() : "none"
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Patient> getAllPatients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Fetching all patients...");
        System.out.println("Current user: " + (auth != null ? auth.getName() : "null"));
        System.out.println("Authorities: " + (auth != null ? auth.getAuthorities() : "null"));
        List<Patient> patients = patientService.getAllPatients();
        System.out.println("Found " + patients.size() + " patients");
        return patients;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Patient> getPatientById(@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Optional<Patient> patient = patientService.getPatientById(id);
        return patient.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Creating patient: " + patient.getName() + " by user: " + (auth != null ? auth.getName() : "anonymous"));
            Patient savedPatient = patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
        } catch (Exception e) {
            System.err.println("Error creating patient: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Eroare internă la salvarea pacientului: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Patient> updatePatient(@PathVariable("id") Integer id, @RequestBody Patient patient) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Optional<Patient> updatedPatient = patientService.updatePatient(id, patient);
        return updatedPatient.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        boolean deleted = patientService.deletePatient(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}