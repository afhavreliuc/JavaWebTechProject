package com.unibuc.management.services;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.Payment;
import com.unibuc.management.entities.ServiceCoverage;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.PatientRepository;
import com.unibuc.management.repositories.PaymentRepository;
import com.unibuc.management.repositories.ServiceCoverageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private final PatientRepository patientRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(PatientRepository patientRepository,
                          MedicalServiceRepository medicalServiceRepository,
                          ServiceCoverageRepository serviceCoverageRepository,
                          PaymentRepository paymentRepository) {
        this.patientRepository = patientRepository;
        this.medicalServiceRepository = medicalServiceRepository;
        this.serviceCoverageRepository = serviceCoverageRepository;
        this.paymentRepository = paymentRepository;
    }

    public Payment createPaymentForPatient(Patient patient, MedicalService service, Appointment appointment) {

        double basePrice = service.getPrice();

        if (Boolean.TRUE.equals(patient.getSubscription())) {
            basePrice = 0.0;
        }
        else if (patient.getInsuranceProvider() != null) {
            Optional<ServiceCoverage> coverageOpt =
                    serviceCoverageRepository.findByMedicalServiceIdAndInsuranceProviderId(
                            service.getId(),
                            patient.getInsuranceProvider().getId()
                    );

            if (coverageOpt.isPresent()) {
                int percent = coverageOpt.get().getCoveragePercent();
                basePrice = basePrice * (100 - percent) / 100;
            }
        }

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(basePrice));
        payment.setPaymentMethod("Cash");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAppointment(appointment);

        return payment;
    }
}