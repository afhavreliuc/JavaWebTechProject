package com.unibuc.management.services;

import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.User;
import com.unibuc.management.repositories.PatientRepository;
import com.unibuc.management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Integer id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByUsername(String username) {
        return patientRepository.findByUserUsername(username);
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Optional<Patient> updatePatient(Integer id, Patient patient) {
        if (patientRepository.existsById(id)) {
            patient.setId(id);
            return Optional.of(patientRepository.save(patient));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deletePatient(Integer id) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            User user = patient.getUser();
            
            // Delete the patient first
            patientRepository.deleteById(id);
            
            // Delete the associated user account if it exists
            if (user != null) {
                userRepository.delete(user);
            }
            
            return true;
        }
        return false;
    }
}