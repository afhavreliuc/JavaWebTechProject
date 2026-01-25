package com.unibuc.management.services;

import com.unibuc.management.entities.Doctor;
import com.unibuc.management.repositories.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

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

    public boolean deleteDoctor(Integer id) {
        Doctor existingDoctor = doctorRepository.findById(id).orElse(null);
        if (existingDoctor == null) return false;

        doctorRepository.delete(existingDoctor);
        return true;
    }

    public Optional<Doctor> getDoctorByMedicalService(Integer medicalServiceId) {
        return doctorRepository.findByMedicalServiceId(medicalServiceId); // Assuming there's a relation
    }

    public Optional<Doctor> getDoctorByUsername(String username) {
        return doctorRepository.findByUserUsername(username);
    }
}