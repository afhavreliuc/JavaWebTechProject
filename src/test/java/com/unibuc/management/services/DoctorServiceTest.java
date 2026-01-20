package com.unibuc.management.services;

import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private Doctor doctorToUpdate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock data for Doctor
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Room 101");
        doctor.setNumberOfPtodays(10);
        doctor.setMedicalService(new MedicalService()); // Mock MedicalService

        doctorToUpdate = new Doctor();
        doctorToUpdate.setId(1);
        doctorToUpdate.setOffice("Room 202");
        doctorToUpdate.setNumberOfPtodays(15);
        doctorToUpdate.setMedicalService(new MedicalService()); // Mock MedicalService
    }

    @Test
    void testSaveDoctor() {
        // Mock the repository save method
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // Call the service method
        Doctor savedDoctor = doctorService.saveDoctor(doctor);

        // Verify the result
        assertNotNull(savedDoctor);
        assertEquals(doctor.getId(), savedDoctor.getId());
        assertEquals(doctor.getOffice(), savedDoctor.getOffice());
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void testGetDoctorById_Found() {
        // Mock the repository to return the doctor when ID is provided
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        // Call the service method
        Doctor foundDoctor = doctorService.getDoctorById(1);

        // Verify the result
        assertNotNull(foundDoctor);
        assertEquals(doctor.getId(), foundDoctor.getId());
        verify(doctorRepository, times(1)).findById(1);
    }

    @Test
    void testGetDoctorById_NotFound() {
        // Mock the repository to return an empty Optional when ID is not found
        when(doctorRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method and verify that an exception is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> doctorService.getDoctorById(1));
        assertEquals("Doctor not found", exception.getMessage());
    }

    @Test
    void testGetAllDoctors() {
        // Mock the repository to return a set of doctors
        Set<Doctor> doctors = Set.of(doctor);
        when(doctorRepository.findAll()).thenReturn((List<Doctor>) doctors);

        // Call the service method
        Set<Doctor> allDoctors = doctorService.getAllDoctors();

        // Verify the result
        assertNotNull(allDoctors);
        assertEquals(1, allDoctors.size());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void testUpdateDoctor_Found() {
        // Mock the repository to return the existing doctor
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctorToUpdate);

        // Call the service method to update doctor details
        Doctor updatedDoctor = doctorService.updateDoctor(1, doctorToUpdate);

        // Verify the result
        assertNotNull(updatedDoctor);
        assertEquals(doctorToUpdate.getOffice(), updatedDoctor.getOffice());
        assertEquals(doctorToUpdate.getNumberOfPtodays(), updatedDoctor.getNumberOfPtodays());
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void testUpdateDoctor_NotFound() {
        // Mock the repository to return an empty Optional when ID is not found
        when(doctorRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method and verify the result is null (doctor not found)
        Doctor updatedDoctor = doctorService.updateDoctor(1, doctorToUpdate);
        assertNull(updatedDoctor);
        verify(doctorRepository, times(0)).save(doctor);
    }

    @Test
    void testDeleteDoctor_Found() {
        // Mock the repository to return the doctor when ID is provided
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        // Call the service method
        boolean deleted = doctorService.deleteDoctor(1);

        // Verify the result
        assertTrue(deleted);
        verify(doctorRepository, times(1)).delete(doctor);
    }

    @Test
    void testDeleteDoctor_NotFound() {
        // Mock the repository to return an empty Optional when ID is not found
        when(doctorRepository.findById(1)).thenReturn(Optional.empty());

        // Call the service method
        boolean deleted = doctorService.deleteDoctor(1);

        // Verify the result
        assertFalse(deleted);
        verify(doctorRepository, times(0)).delete(doctor);
    }

    @Test
    void testGetDoctorByMedicalService() {
        // Mock the repository to return a doctor with a medical service
        when(doctorRepository.findByMedicalServiceId(1)).thenReturn(Optional.of(doctor));

        // Call the service method
        Optional<Doctor> doctorByMedicalService = doctorService.getDoctorByMedicalService(1);

        // Verify the result
        assertTrue(doctorByMedicalService.isPresent());
        assertEquals(doctor.getId(), doctorByMedicalService.get().getId());
        verify(doctorRepository, times(1)).findByMedicalServiceId(1);
    }

    @Test
    void testGetDoctorByMedicalService_NotFound() {
        // Mock the repository to return an empty Optional when medical service is not found
        when(doctorRepository.findByMedicalServiceId(1)).thenReturn(Optional.empty());

        // Call the service method
        Optional<Doctor> doctorByMedicalService = doctorService.getDoctorByMedicalService(1);

        // Verify the result
        assertFalse(doctorByMedicalService.isPresent());
        verify(doctorRepository, times(1)).findByMedicalServiceId(1);
    }
}

