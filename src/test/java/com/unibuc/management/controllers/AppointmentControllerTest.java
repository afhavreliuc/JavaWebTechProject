package com.unibuc.management.controllers;

import com.unibuc.management.entities.*;
import com.unibuc.management.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PatientService patientService;

    @Mock
    private MedicalServiceService medicalServiceService;

    @Mock
    private PaymentService paymentTypeService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    public void testCreateAppointment_Success() throws Exception {
        // Mock data
        Integer patientId = 1;
        Integer medicalServiceId = 1;
        OffsetDateTime appointmentFrom = OffsetDateTime.now().plusDays(1);

        Patient patient = new Patient();
        MedicalService medicalService = new MedicalService();
        PaymentType paymentType = new PaymentType();
        Doctor doctor = new Doctor();

        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));
        when(medicalServiceService.getMedicalServiceById(medicalServiceId)).thenReturn(Optional.of(medicalService));
        when(doctorService.getDoctorByMedicalService(medicalServiceId)).thenReturn(Optional.of(doctor));
        when(paymentTypeService.getPriceForPatient(medicalServiceId, patientId)).thenReturn(Optional.of(paymentType));

        List<OffsetDateTime> availableSlots = Arrays.asList(appointmentFrom);
        when(appointmentService.getAvailableTimeSlots(medicalService, appointmentFrom.toLocalDate().toString())).thenReturn(availableSlots);

        Appointment appointment = new Appointment();
        appointment.setAppointmentFrom(appointmentFrom);
        when(appointmentService.createAppointment(appointment)).thenReturn(appointment);

        // Perform the request
        mockMvc.perform(post("/api/appointments")
                        .param("patientId", String.valueOf(patientId))
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("appointmentFrom", appointmentFrom.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentFrom").value(appointmentFrom.toString()));
    }

    @Test
    public void testCreateAppointment_Failure_InvalidTime() throws Exception {
        Integer patientId = 1;
        Integer medicalServiceId = 1;
        OffsetDateTime appointmentFrom = OffsetDateTime.now().plusDays(1);

        Patient patient = new Patient();
        MedicalService medicalService = new MedicalService();
        PaymentType paymentType = new PaymentType();

        when(patientService.getPatientById(patientId)).thenReturn(Optional.of(patient));
        when(medicalServiceService.getMedicalServiceById(medicalServiceId)).thenReturn(Optional.of(medicalService));
        when(paymentTypeService.getPriceForPatient(medicalServiceId, patientId)).thenReturn(Optional.of(paymentType));

        List<OffsetDateTime> availableSlots = Collections.emptyList();
        when(appointmentService.getAvailableTimeSlots(medicalService, appointmentFrom.toLocalDate().toString())).thenReturn(availableSlots);

        // Perform the request
        mockMvc.perform(post("/api/appointments")
                        .param("patientId", String.valueOf(patientId))
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("appointmentFrom", appointmentFrom.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAvailableTimeSlots_Success() throws Exception {
        Integer medicalServiceId = 1;
        String date = "2025-01-15";
        MedicalService medicalService = new MedicalService();
        OffsetDateTime availableSlot = OffsetDateTime.parse(date + "T09:00:00+00:00");

        when(medicalServiceService.getMedicalServiceById(medicalServiceId)).thenReturn(Optional.of(medicalService));
        when(appointmentService.getAvailableTimeSlots(medicalService, date)).thenReturn(Collections.singletonList(availableSlot));

        mockMvc.perform(get("/api/appointments/available-times")
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(availableSlot.toString()));
    }

    @Test
    public void testGetAvailableTimeSlots_Failure() throws Exception {
        Integer medicalServiceId = 1;
        String date = "2025-01-15";

        when(medicalServiceService.getMedicalServiceById(medicalServiceId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/appointments/available-times")
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("date", date))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAppointmentsByPatientId() throws Exception {
        Integer patientId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(1);

        when(appointmentService.getAppointmentsByPatientId(patientId)).thenReturn(Collections.singletonList(appointment));

        mockMvc.perform(get("/api/appointments/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testSubmitFeedback_Success() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;
        Appointment appointment = new Appointment();
        appointment.setAppointmentFrom(OffsetDateTime.now().minusMinutes(35));

        MedicalService service = new MedicalService();
        service.setRating(3.5);
        service.setNrOfRatings(5);

        when(appointmentService.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(medicalServiceService.save(service)).thenReturn(service);

        mockMvc.perform(post("/api/appointments/{appointmentId}/feedback", appointmentId)
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isOk())
                .andExpect(content().string("Feedback submitted successfully."));
    }

    @Test
    public void testSubmitFeedback_Failure_TooSoon() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;
        Appointment appointment = new Appointment();
        appointment.setAppointmentFrom(OffsetDateTime.now().plusMinutes(10));

        when(appointmentService.findById(appointmentId)).thenReturn(Optional.of(appointment));

        mockMvc.perform(post("/api/appointments/{appointmentId}/feedback", appointmentId)
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Feedback can only be submitted after 30 minutes from the appointment start time."));
    }

    @Test
    public void testSubmitFeedback_Failure_AppointmentNotFound() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;

        when(appointmentService.findById(appointmentId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/appointments/{appointmentId}/feedback", appointmentId)
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Appointment not found or already completed."));
    }
}
