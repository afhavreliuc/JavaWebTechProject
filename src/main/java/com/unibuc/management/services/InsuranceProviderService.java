package com.unibuc.management.services;

import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InsuranceProviderService {

    private final InsuranceProviderRepository insuranceProviderRepository;

    @Autowired
    public InsuranceProviderService(InsuranceProviderRepository insuranceProviderRepository) {
        this.insuranceProviderRepository = insuranceProviderRepository;
    }

    public List<InsuranceProvider> getAllInsuranceProviders() {
        return insuranceProviderRepository.findAll();
    }

    public Optional<InsuranceProvider> getInsuranceProviderById(Integer id) {
        return insuranceProviderRepository.findById(id);
    }

    public InsuranceProvider createInsuranceProvider(InsuranceProvider insuranceProvider) {
        return insuranceProviderRepository.save(insuranceProvider);
    }

    public Optional<InsuranceProvider> updateInsuranceProvider(Integer id, InsuranceProvider insuranceProvider) {
        if (insuranceProviderRepository.existsById(id)) {
            insuranceProvider.setId(id);
            return Optional.of(insuranceProviderRepository.save(insuranceProvider));
        }
        return Optional.empty();
    }

    public boolean deleteInsuranceProvider(Integer id) {
        if (insuranceProviderRepository.existsById(id)) {
            insuranceProviderRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

