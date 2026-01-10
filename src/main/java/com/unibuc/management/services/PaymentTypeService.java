package com.unibuc.management.services;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.PaymentType;
import com.unibuc.management.repositories.PaymentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final MedicalServiceService medicalServiceService;  // Assuming you have a service for MedicalService
    private final PatientService patientService;  // Assuming you have a service for Patient

    @Autowired
    public PaymentTypeService(PaymentTypeRepository paymentTypeRepository,
                              MedicalServiceService medicalServiceService,
                              PatientService patientService) {
        this.paymentTypeRepository = paymentTypeRepository;
        this.medicalServiceService = medicalServiceService;
        this.patientService = patientService;
    }


    public Optional<PaymentType> getPaymentTypeById(Integer id) {
        return paymentTypeRepository.findById(id);
    }

    public Optional<PaymentType> getPriceForPatient(Integer medicalServiceId, Integer patientId) {
        Optional<MedicalService> medicalServiceOpt = medicalServiceService.getMedicalServiceById(medicalServiceId);
        Optional<Patient> patientOpt = patientService.getPatientById(patientId);

        if (medicalServiceOpt.isPresent() && patientOpt.isPresent()) {
            MedicalService medicalService = medicalServiceOpt.get();
            Patient patient = patientOpt.get();

            if (patient.getInsurance()) {
                Optional<PaymentType> paymentType = paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(),true, false);
                return paymentType;// Return the insurance price
            }

            if (patient.getActiveSubscription() != null) {
                Optional<PaymentType> paymentType = paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(),false, true);  // Return the insurance price
                return paymentType;
            }

            Optional<PaymentType> paymentType = paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(),false, false);
            return paymentType;
        }
        return null;
    }
}