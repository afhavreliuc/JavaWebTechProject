package com.unibuc.management.services;

import com.unibuc.management.entities.*;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaidTimeOffRepository ptoRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private MedicalService medicalService;
    private LocalDate selectedDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appointmentService = new AppointmentService(appointmentRepository, ptoRepository, doctorRepository);

        // Setup mock data
        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setStartHour(9); // e.g., appointment starts at 9 AM
        medicalService.setEndHour(17); // e.g., appointment ends at 5 PM

        selectedDate = LocalDate.of(2025, 1, 14); // e.g., set a date for the appointment slot
    }

    @Test
    void testCreateAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setMedicalService(medicalService);

        // Mock the repository to return the saved appointment
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment savedAppointment = appointmentService.createAppointment(appointment);

        assertNotNull(savedAppointment);
        assertEquals(1, savedAppointment.getId());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void testGetAvailableTimeSlots() {
        // Setup a mock medical service with a valid time window
        String date = "2025-01-14";

        // Mock the AppointmentRepository to return existing appointments
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentFrom(OffsetDateTime.of(2025, 1, 14, 9, 0, 0, 0, ZoneOffset.UTC));
        when(appointmentRepository.findByMedicalServiceAndDate(eq(medicalService.getId()), any(), any()))
                .thenReturn(Collections.singletonList(existingAppointment));

        // Mock the PaidTimeOffRepository to return any PTOs
        PaidTimeOff pto = new PaidTimeOff();
        pto.setPtoFrom(OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC));
        pto.setPtoTo(OffsetDateTime.of(2025, 1, 14, 12, 0, 0, 0, ZoneOffset.UTC));
        when(ptoRepository.findByDoctorAndDate(eq(1), any(), any())).thenReturn(Collections.singletonList(pto));

        // Mock the DoctorRepository to return a doctor
        Doctor doctor = new Doctor();
        doctor.setId(1);
        when(doctorRepository.findByMedicalServiceId(eq(medicalService.getId()))).thenReturn(Optional.of(doctor));

        List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(medicalService, date);

        // Check if available slots are as expected
        assertNotNull(availableSlots);
        assertTrue(availableSlots.size() > 0);  // Check that there are available slots
        assertEquals(OffsetDateTime.of(2025, 1, 14, 9, 30, 0, 0, ZoneOffset.UTC), availableSlots.get(0));
        assertEquals(OffsetDateTime.of(2025, 1, 14, 12, 30, 0, 0, ZoneOffset.UTC), availableSlots.get(availableSlots.size() - 1));
    }

    @Test
    void testGetAppointmentsByPatientId() {
        Integer patientId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(patientId);

        List<Appointment> appointments = Collections.singletonList(appointment);

        when(appointmentRepository.findByPatientId(patientId)).thenReturn(appointments);

        List<Appointment> result = appointmentService.getAppointmentsByPatientId(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(patientId, result.get(0).getId());
    }

    @Test
    void testFindById_Success() {
        Integer appointmentId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        Optional<Appointment> foundAppointment = appointmentService.findById(appointmentId);

        assertTrue(foundAppointment.isPresent());
        assertEquals(appointmentId, foundAppointment.get().getId());
    }

    @Test
    void testFindById_NotFound() {
        Integer appointmentId = 1;

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        Optional<Appointment> foundAppointment = appointmentService.findById(appointmentId);

        assertFalse(foundAppointment.isPresent());
    }

    @Test
    void testSaveAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setMedicalService(medicalService);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment savedAppointment = appointmentService.save(appointment);

        assertNotNull(savedAppointment);
        assertEquals(1, savedAppointment.getId());
        verify(appointmentRepository, times(1)).save(appointment);
    }
}
