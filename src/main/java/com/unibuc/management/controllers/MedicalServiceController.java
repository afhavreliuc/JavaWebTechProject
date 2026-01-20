package com.unibuc.management.controllers;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/medical-services")
public class MedicalServiceController {

    @Autowired
    private MedicalServiceRepository medicalServiceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping
    public ResponseEntity<List<MedicalService>> getAllMedicalServices() {
        List<MedicalService> services = medicalServiceRepository.findAll();
        return ResponseEntity.ok(services);
    }
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<MedicalService>> getMedicalServicesBySpecialization(@PathVariable String specialization) {
        List<MedicalService> services = medicalServiceRepository.findBySpecialization(specialization);
        return ResponseEntity.ok(services);
    }


}