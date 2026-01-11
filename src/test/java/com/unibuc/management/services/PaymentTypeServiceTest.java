package com.unibuc.management.services;

import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.PaymentType;
import com.unibuc.management.entities.SubscriptionPlan;
import com.unibuc.management.repositories.PaymentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentTypeServiceTest {

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private MedicalServiceService medicalServiceService;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PaymentTypeService paymentTypeService;

    private MedicalService medicalService;
    private Patient patient;
    private PaymentType paymentType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock entities
        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("General Checkup");

        patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");
        patient.setInsuranceProvider(new InsuranceProvider());
        patient.setSubscription(false);
        patient.setActiveSubscription(null);

        paymentType = new PaymentType();
        paymentType.setId(1);
        paymentType.setMedicalService(medicalService);
        paymentType.setWithInsurance(true);
        paymentType.setWithSubscription(false);
    }

    @Test
    void testGetPaymentTypeById_Found() {
        // Test when PaymentType is found
        when(paymentTypeRepository.findById(1)).thenReturn(Optional.of(paymentType));

        // Call the service method
        Optional<PaymentType> foundPaymentType = paymentTypeService.getPaymentTypeById(1);

        // Verify the result
        assertTrue(foundPaymentType.isPresent());
        assertEquals(paymentType.getId(), foundPaymentType.get().getId());
        verify(paymentTypeRepository, times(1)).findById(1);
    }

    @Test
    void testGetPaymentTypeById_NotFound() {
        // Test when PaymentType is not found
        when(paymentTypeRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method
        Optional<PaymentType> foundPaymentType = paymentTypeService.getPaymentTypeById(1);

        // Verify the result
        assertFalse(foundPaymentType.isPresent());
        verify(paymentTypeRepository, times(1)).findById(1);
    }

    @Test
    void testGetPriceForPatient_WithInsurance() {
        // Test when patient has insurance
        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(Optional.of(medicalService));
        when(patientService.getPatientById(1)).thenReturn(Optional.of(patient));
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, true, false))
                .thenReturn(Optional.of(paymentType));

        // Call the service method
        Optional<PaymentType> result = paymentTypeService.getPriceForPatient(1, 1);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(paymentType.getId(), result.get().getId());
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
        verify(paymentTypeRepository, times(1))
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, true, false);
    }

    @Test
    void testGetPriceForPatient_WithSubscription() {
        // Test when patient has subscription but no insurance
        patient.setInsuranceProvider(null);
        patient.setSubscription(true);
        patient.setActiveSubscription(new SubscriptionPlan());

        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(Optional.of(medicalService));
        when(patientService.getPatientById(1)).thenReturn(Optional.of(patient));
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, true))
                .thenReturn(Optional.of(paymentType));

        // Call the service method
        Optional<PaymentType> result = paymentTypeService.getPriceForPatient(1, 1);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(paymentType.getId(), result.get().getId());
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
        verify(paymentTypeRepository, times(1))
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, true);
    }

    @Test
    void testGetPriceForPatient_NoInsuranceNoSubscription() {
        // Test when patient has neither insurance nor subscription
        patient.setInsuranceProvider(null);
        patient.setSubscription(false);
        patient.setActiveSubscription(null);

        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(Optional.of(medicalService));
        when(patientService.getPatientById(1)).thenReturn(Optional.of(patient));
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, false))
                .thenReturn(Optional.of(paymentType));

        // Call the service method
        Optional<PaymentType> result = paymentTypeService.getPriceForPatient(1, 1);

        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(paymentType.getId(), result.get().getId());
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
        verify(paymentTypeRepository, times(1))
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, false);
    }

    @Test
    void testGetPriceForPatient_NotFound() {
        // Test when MedicalService or Patient is not found
        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(Optional.empty());
        when(patientService.getPatientById(1)).thenReturn(Optional.of(patient));

        // Call the service method
        Optional<PaymentType> result = paymentTypeService.getPriceForPatient(1, 1);

        // Verify the result
        assertNull(result);
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
    }

    @Test
    void testGetPriceForPatient_NoPaymentTypeFound() {
        // Test when there is no PaymentType found in repository
        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(Optional.of(medicalService));
        when(patientService.getPatientById(1)).thenReturn(Optional.of(patient));
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, true, false))
                .thenReturn(Optional.empty());

        // Call the service method
        Optional<PaymentType> result = paymentTypeService.getPriceForPatient(1, 1);

        // Verify the result
        assertFalse(result.isPresent());
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
        verify(paymentTypeRepository, times(1))
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, true, false);
    }
}

