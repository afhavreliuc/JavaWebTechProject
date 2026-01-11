package com.unibuc.management.controllers;

import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.services.InsuranceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/insurance-providers")
public class InsuranceProviderController {

    private final InsuranceProviderService insuranceProviderService;

    @Autowired
    public InsuranceProviderController(InsuranceProviderService insuranceProviderService) {
        this.insuranceProviderService = insuranceProviderService;
    }

    @GetMapping
    public List<InsuranceProvider> getAllInsuranceProviders() {
        return insuranceProviderService.getAllInsuranceProviders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProvider> getInsuranceProviderById(@PathVariable Integer id) {
        return insuranceProviderService.getInsuranceProviderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InsuranceProvider> createInsuranceProvider(@RequestBody InsuranceProvider insuranceProvider) {
        InsuranceProvider saved = insuranceProviderService.createInsuranceProvider(insuranceProvider);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsuranceProvider> updateInsuranceProvider(@PathVariable Integer id, @RequestBody InsuranceProvider insuranceProvider) {
        return insuranceProviderService.updateInsuranceProvider(id, insuranceProvider)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsuranceProvider(@PathVariable Integer id) {
        if (insuranceProviderService.deleteInsuranceProvider(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

