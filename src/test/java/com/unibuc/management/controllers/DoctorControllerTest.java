package com.unibuc.management.controllers;

import com.unibuc.management.entities.Doctor;
import com.unibuc.management.services.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class DoctorControllerTest {

    @InjectMocks
    private DoctorController doctorController;

    @Mock
    private DoctorService doctorService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController).build();
    }

    @Test
    public void createDoctor_ReturnsCreatedStatus() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Cardiology");

        when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(doctor);

        mockMvc.perform(post("/api/doctors")
                        .contentType("application/json")
                        .content("{\"office\": \"Cardiology\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.office").value("Cardiology"));
    }

    @Test
    public void getDoctorById_ReturnsDoctor_WhenFound() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Cardiology");

        when(doctorService.getDoctorById(1)).thenReturn(doctor);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.office").value("Cardiology"));
    }

    @Test
    public void getDoctorById_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        when(doctorService.getDoctorById(1)).thenReturn(null);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateDoctor_ReturnsUpdatedDoctor_WhenDoctorExists() throws Exception {
        Doctor existingDoctor = new Doctor();
        existingDoctor.setId(1);
        existingDoctor.setOffice("Cardiology");

        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId(1);
        updatedDoctor.setOffice("Neurology");

        when(doctorService.updateDoctor(1, updatedDoctor)).thenReturn(updatedDoctor);

        mockMvc.perform(put("/api/doctors/1")
                        .contentType("application/json")
                        .content("{\"office\": \"Neurology\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.office").value("Neurology"));
    }

    @Test
    public void updateDoctor_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId(1);
        updatedDoctor.setOffice("Neurology");

        when(doctorService.updateDoctor(1, updatedDoctor)).thenReturn(null);

        mockMvc.perform(put("/api/doctors/1")
                        .contentType("application/json")
                        .content("{\"office\": \"Neurology\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteDoctor_ReturnsNoContent_WhenDeleted() throws Exception {
        when(doctorService.deleteDoctor(1)).thenReturn(true);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteDoctor_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        when(doctorService.deleteDoctor(1)).thenReturn(false);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNotFound());
    }
}
