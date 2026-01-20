package com.unibuc.management.services;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalServiceServiceTest {

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @InjectMocks
    private MedicalServiceService medicalServiceService;

    private MedicalService medicalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a mock medical service
        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("General Consultation");
    }

    @Test
    void testGetMedicalServiceById_Found() {
        // Mock the repository to return the medical service when the ID is found
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        // Call the service method
        Optional<MedicalService> foundService = medicalServiceService.getMedicalServiceById(1);

        // Verify the result
        assertTrue(foundService.isPresent());
        assertEquals(medicalService.getId(), foundService.get().getId());
        assertEquals(medicalService.getName(), foundService.get().getName());
        verify(medicalServiceRepository, times(1)).findById(1);
    }

    @Test
    void testGetMedicalServiceById_NotFound() {
        // Mock the repository to return an empty Optional when the ID is not found
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method
        Optional<MedicalService> foundService = medicalServiceService.getMedicalServiceById(1);

        // Verify the result
        assertFalse(foundService.isPresent());
        verify(medicalServiceRepository, times(1)).findById(1);
    }

    @Test
    void testSaveMedicalService() {
        // Mock the repository to return the saved medical service
        when(medicalServiceRepository.save(medicalService)).thenReturn(medicalService);

        // Call the service method to save the medical service
        MedicalService savedService = medicalServiceService.save(medicalService);

        // Verify the result
        assertNotNull(savedService);
        assertEquals(medicalService.getId(), savedService.getId());
        assertEquals(medicalService.getName(), savedService.getName());
        verify(medicalServiceRepository, times(1)).save(medicalService);
    }
}
