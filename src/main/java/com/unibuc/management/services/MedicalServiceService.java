package com.unibuc.management.services;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;

    @Autowired
    public MedicalServiceService(MedicalServiceRepository medicalServiceRepository) {
        this.medicalServiceRepository = medicalServiceRepository;
    }

    public Optional<MedicalService> getMedicalServiceById(Integer id) {
        return medicalServiceRepository.findById(id);
    }
    public MedicalService save(MedicalService service) {
        return medicalServiceRepository.save(service); // Save the medical service
    }

}
