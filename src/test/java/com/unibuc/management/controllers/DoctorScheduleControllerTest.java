package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.services.DoctorScheduleService;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class DoctorScheduleControllerTest {

    @InjectMocks
    private DoctorScheduleController doctorScheduleController;

    @Mock
    private DoctorScheduleService doctorScheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorScheduleController).build();
    }

    @Test
    public void getDoctorScheduleForDay_ReturnsSchedule() throws Exception {
        // Sample data
        OffsetDateTime fromTime1 = OffsetDateTime.parse("2025-01-14T09:00:00+00:00");
        OffsetDateTime toTime1 = OffsetDateTime.parse("2025-01-14T09:30:00+00:00");

        OffsetDateTime fromTime2 = OffsetDateTime.parse("2025-01-14T10:00:00+00:00");
        OffsetDateTime toTime2 = OffsetDateTime.parse("2025-01-14T10:30:00+00:00");

        Doctor doctor = new Doctor();  // assuming Doctor is a predefined class
        ScheduleEntry entry1 = new ScheduleEntry("Doctor's Appointment", fromTime1, toTime1, doctor);
        ScheduleEntry entry2 = new ScheduleEntry("Doctor's Appointment", fromTime2, toTime2, doctor);
        List<ScheduleEntry> schedule = Arrays.asList(entry1, entry2);

        // Mock the service method
        when(doctorScheduleService.getDoctorScheduleForDay(1, LocalDate.of(2025, 1, 14)))
                .thenReturn(schedule);

// Perform the GET request
        mockMvc.perform(get("/api/doctor-schedule/day")
                        .param("doctorId", "1")
                        .param("date", "2025-01-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from").value("2025-01-14T09:00:00+00:00"))
                .andExpect(jsonPath("$[0].to").value("2025-01-14T09:30:00+00:00"))
                .andExpect(jsonPath("$[1].from").value("2025-01-14T10:00:00+00:00"))
                .andExpect(jsonPath("$[1].to").value("2025-01-14T10:30:00+00:00"));
    }

    @Test
    public void schedulePTO_Success_ReturnsSuccessMessage() throws Exception {
        // Mock service response
        when(doctorScheduleService.schedulePTO(1, OffsetDateTime.parse("2025-01-14T10:00:00+00:00"),
                OffsetDateTime.parse("2025-01-14T12:00:00+00:00")))
                .thenReturn("PTO scheduled successfully.");

        // Perform the POST request
        mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                        .param("doctorId", "1")
                        .param("startDate", "2025-01-14T10:00:00+00:00")
                        .param("endDate", "2025-01-14T12:00:00+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("PTO scheduled successfully."));
    }

    @Test
    public void schedulePTO_Failure_ReturnsBadRequest() throws Exception {
        // Mock service response
        when(doctorScheduleService.schedulePTO(1, OffsetDateTime.parse("2025-01-14T10:00:00+00:00"),
                OffsetDateTime.parse("2025-01-14T12:00:00+00:00")))
                .thenReturn("Failed to schedule PTO due to overlapping appointments.");

        // Perform the POST request
        mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                        .param("doctorId", "1")
                        .param("startDate", "2025-01-14T10:00:00+00:00")
                        .param("endDate", "2025-01-14T12:00:00+00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Failed to schedule PTO due to overlapping appointments."));
    }
}
