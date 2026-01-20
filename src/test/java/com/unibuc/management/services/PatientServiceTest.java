package com.unibuc.management.services;

import com.unibuc.management.entities.Patient;
import com.unibuc.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a mock patient
        patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");
    }

    @Test
    void testGetAllPatients() {
        // Test the getAllPatients() method
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        // Call the service method
        List<Patient> patients = patientService.getAllPatients();

        // Verify the result
        assertNotNull(patients);
        assertFalse(patients.isEmpty());
        assertEquals(1, patients.size());
        assertEquals(patient.getId(), patients.get(0).getId());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById_Found() {
        // Test getPatientById() when patient is found
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        // Call the service method
        Optional<Patient> foundPatient = patientService.getPatientById(1);

        // Verify the result
        assertTrue(foundPatient.isPresent());
        assertEquals(patient.getId(), foundPatient.get().getId());
        assertEquals(patient.getName(), foundPatient.get().getName());
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testGetPatientById_NotFound() {
        // Test getPatientById() when patient is not found
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method
        Optional<Patient> foundPatient = patientService.getPatientById(1);

        // Verify the result
        assertFalse(foundPatient.isPresent());
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testCreatePatient() {
        // Test createPatient() method
        when(patientRepository.save(patient)).thenReturn(patient);

        // Call the service method to create the patient
        Patient createdPatient = patientService.createPatient(patient);

        // Verify the result
        assertNotNull(createdPatient);
        assertEquals(patient.getId(), createdPatient.getId());
        assertEquals(patient.getName(), createdPatient.getName());
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void testUpdatePatient_Found() {
        // Test updatePatient() method when patient exists
        when(patientRepository.existsById(1)).thenReturn(true);
        when(patientRepository.save(patient)).thenReturn(patient);

        // Call the service method to update the patient
        Optional<Patient> updatedPatient = patientService.updatePatient(1, patient);

        // Verify the result
        assertTrue(updatedPatient.isPresent());
        assertEquals(patient.getId(), updatedPatient.get().getId());
        assertEquals(patient.getName(), updatedPatient.get().getName());
        verify(patientRepository, times(1)).existsById(1);
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void testUpdatePatient_NotFound() {
        // Test updatePatient() method when patient does not exist
        when(patientRepository.existsById(1)).thenReturn(false);

        // Call the service method to update the patient
        Optional<Patient> updatedPatient = patientService.updatePatient(1, patient);

        // Verify the result
        assertFalse(updatedPatient.isPresent());
        verify(patientRepository, times(1)).existsById(1);
    }

    @Test
    void testDeletePatient_Success() {
        // Test deletePatient() method when patient exists
        when(patientRepository.existsById(1)).thenReturn(true);

        // Call the service method to delete the patient
        boolean deleted = patientService.deletePatient(1);

        // Verify the result
        assertTrue(deleted);
        verify(patientRepository, times(1)).existsById(1);
        verify(patientRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeletePatient_NotFound() {
        // Test deletePatient() method when patient does not exist
        when(patientRepository.existsById(1)).thenReturn(false);

        // Call the service method to delete the patient
        boolean deleted = patientService.deletePatient(1);

        // Verify the result
        assertFalse(deleted);
        verify(patientRepository, times(1)).existsById(1);
    }
}
