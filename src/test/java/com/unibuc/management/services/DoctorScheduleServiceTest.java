package com.unibuc.management.services;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.*;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class DoctorScheduleServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaidTimeOffRepository ptoRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorScheduleService doctorScheduleService;

    private Doctor doctor;
    private LocalDate selectedDate;
    private OffsetDateTime ptoStart;
    private OffsetDateTime ptoEnd;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctorScheduleService = new DoctorScheduleService(appointmentRepository, ptoRepository, doctorRepository);

        // Setup mock data
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setMedicalService(new MedicalService());  // Assuming doctor has a valid medical service
        selectedDate = LocalDate.of(2025, 1, 14); // e.g., set a date for the appointment slot

        // Setup mock Paid Time Off
        ptoStart = OffsetDateTime.of(2025, 1, 14, 9, 0, 0, 0, ZoneOffset.UTC);
        ptoEnd = OffsetDateTime.of(2025, 1, 14, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void testGetDoctorScheduleForDay() {
        // Prepare mock appointments and PTO
        Appointment appointment = new Appointment();
        appointment.setAppointmentFrom(OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC));

        PaidTimeOff pto = new PaidTimeOff();
        pto.setPtoFrom(ptoStart);
        pto.setPtoTo(ptoEnd);

        // Mock the repositories
        when(appointmentRepository.findByMedicalServiceAndDate(eq(doctor.getMedicalService().getId()), any(), any()))
                .thenReturn(Collections.singletonList(appointment));
        when(ptoRepository.findByDoctorAndDate(eq(doctor.getId()), any(), any()))
                .thenReturn(Collections.singletonList(pto));

        // Call the service method
        List<ScheduleEntry> schedule = doctorScheduleService.getDoctorScheduleForDay(doctor.getId(), selectedDate);

        // Verify the results
        assertNotNull(schedule);
        assertEquals(2, schedule.size()); // We expect 2 entries (1 appointment and 1 PTO)

        // Check if the appointment is correctly mapped to a ScheduleEntry
        ScheduleEntry appointmentEntry = schedule.get(0);
        assertEquals("Appointment", appointmentEntry.getTitle());
        assertEquals(appointment.getAppointmentFrom(), appointmentEntry.getFrom());

        // Check if the PTO is correctly mapped to a ScheduleEntry
        ScheduleEntry ptoEntry = schedule.get(1);
        assertEquals("Paid Time Off", ptoEntry.getTitle());
        assertEquals(pto.getPtoFrom(), ptoEntry.getFrom());
        assertEquals(pto.getPtoTo(), ptoEntry.getTo());
    }

    @Test
    void testSchedulePTO_Success() {
        // Mock the doctor repository to return a doctor
        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));

        // Mock the appointment repository to return no appointments
        when(appointmentRepository.findByMedicalServiceAndDate(eq(doctor.getMedicalService().getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // Call the service method to schedule PTO
        String result = doctorScheduleService.schedulePTO(doctor.getId(), ptoStart, ptoEnd);

        // Verify the result
        assertEquals("PTO successfully scheduled!", result);
        verify(ptoRepository, times(1)).save(any(PaidTimeOff.class));
    }

    @Test
    void testSchedulePTO_DoctorNotFound() {
        // Mock the doctor repository to return empty (doctor not found)
        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.empty());

        // Call the service method to schedule PTO
        String result = doctorScheduleService.schedulePTO(doctor.getId(), ptoStart, ptoEnd);

        // Verify the result
        assertEquals("Doctor with ID " + doctor.getId() + " not found.", result);
        verify(ptoRepository, times(0)).save(any(PaidTimeOff.class));
    }

    @Test
    void testSchedulePTO_ConflictingAppointments() {
        // Mock the doctor repository to return a valid doctor
        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));

        // Mock the appointment repository to return a conflicting appointment
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentFrom(OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC));
        when(appointmentRepository.findByMedicalServiceAndDate(eq(doctor.getMedicalService().getId()), any(), any()))
                .thenReturn(Collections.singletonList(existingAppointment));

        // Call the service method to schedule PTO
        String result = doctorScheduleService.schedulePTO(doctor.getId(), ptoStart, ptoEnd);

        // Verify the result
        assertEquals("Doctor has appointments on the selected day. PTO cannot be scheduled.", result);
        verify(ptoRepository, times(0)).save(any(PaidTimeOff.class));
    }
}

