package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.services.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllPatients() throws Exception {
        // Simulate a list of patients
        when(patientService.getAllPatients()).thenReturn(List.of(new Patient(), new Patient()));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").isNotEmpty());
    }

    @Test
    void testGetPatientById_Success() throws Exception {
        Integer patientId = 1;
        Patient patient = new Patient();
        patient.setId(patientId);

        // Simulate finding the patient by ID
        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId));
    }

    @Test
    void testGetPatientById_NotFound() throws Exception {
        Integer patientId = 1;

        // Simulate the patient not being found
        when(patientService.getPatientById(patientId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreatePatient() throws Exception {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");

        // Simulate saving the patient
        when(patientService.createPatient(any(Patient.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testUpdatePatient_Success() throws Exception {
        Integer patientId = 1;
        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);

        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);
        updatedPatient.setName("Updated Name");

        // Simulate updating the patient
        when(patientService.updatePatient(patientId, updatedPatient)).thenReturn(Optional.of(updatedPatient));

        mockMvc.perform(put("/api/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testUpdatePatient_NotFound() throws Exception {
        Integer patientId = 1;
        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);

        // Simulate the patient not being found
        when(patientService.updatePatient(patientId, updatedPatient)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPatient)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePatient_Success() throws Exception {
        Integer patientId = 1;

        // Simulate deleting the patient
        when(patientService.deletePatient(patientId)).thenReturn(true);

        mockMvc.perform(delete("/api/patients/{id}", patientId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePatient_NotFound() throws Exception {
        Integer patientId = 1;

        // Simulate the patient not being found
        when(patientService.deletePatient(patientId)).thenReturn(false);

        mockMvc.perform(delete("/api/patients/{id}", patientId))
                .andExpect(status().isNotFound());
    }
}