package com.unibuc.management.services;

import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.User;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor getDoctorById(Integer doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor updateDoctor(Integer id, Doctor doctorDetails) {
        Doctor existingDoctor = doctorRepository.findById(id).orElse(null);
        if (existingDoctor == null) return null;

        existingDoctor.setName(doctorDetails.getName());
        existingDoctor.setOffice(doctorDetails.getOffice());
        existingDoctor.setNumberOfPtodays(doctorDetails.getNumberOfPtodays());
        existingDoctor.setMedicalService(doctorDetails.getMedicalService());

        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public boolean deleteDoctor(Integer id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isEmpty()) {
            return false;
        }
        
        Doctor doctor = doctorOpt.get();
        User user = doctor.getUser();
        
        doctorRepository.delete(doctor);
        
        if (user != null) {
            userRepository.delete(user);
        }
        
        return true;
    }

    public Optional<Doctor> getDoctorByMedicalService(Integer medicalServiceId) {
        return doctorRepository.findByMedicalServiceId(medicalServiceId);
    }

    public Optional<Doctor> getDoctorByUsername(String username) {
        return doctorRepository.findByUserUsername(username);
    }
}