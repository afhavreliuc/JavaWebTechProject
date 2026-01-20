package com.unibuc.management.services;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.PaidTimeOff;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, PaidTimeOffRepository ptoRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.ptoRepository = ptoRepository;
        this.doctorRepository = doctorRepository;
    }
    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    public List<OffsetDateTime> getAvailableTimeSlots(MedicalService medicalService, String date) {
        LocalDate selectedDate = LocalDate.parse(date);
        LocalDateTime startTime = LocalDateTime.of(selectedDate, LocalTime.of(medicalService.getStartHour(), 0));
        LocalDateTime endTime = LocalDateTime.of(selectedDate, LocalTime.of(medicalService.getEndHour(), 0));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        ZoneOffset zoneOffset = ZoneOffset.UTC;

        OffsetDateTime startOfDay = selectedDate.atStartOfDay().atOffset(zoneOffset);
        OffsetDateTime endOfDay = selectedDate.atTime(LocalTime.MAX).atOffset(zoneOffset);

        List<Appointment> existingAppointments = appointmentRepository.findByMedicalServiceAndDate(medicalService.getId(), startOfDay,endOfDay);
        Optional<Doctor> doctorOptional = doctorRepository.findByMedicalServiceId(medicalService.getId());
        List<PaidTimeOff> doctorPTOs = new ArrayList<PaidTimeOff>();
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
         doctorPTOs = ptoRepository.findByDoctorAndDate(doctor.getId(), startOfDay,endOfDay);
        }
        // Generate available 30-minute slots between start and end hours
        List<OffsetDateTime> availableSlots = new ArrayList<>();
        while (startTime.isBefore(endTime)) {
            OffsetDateTime slot = startTime.atOffset(ZoneOffset.UTC);

            boolean isPastTime = slot.isBefore(now);
            boolean isBlockedByPTO = doctorPTOs.stream().anyMatch(pto -> slot.isAfter(pto.getPtoFrom()) && slot.isBefore(pto.getPtoTo()));
            boolean hasAppointment = existingAppointments.stream().anyMatch(appt -> appt.getAppointmentFrom().equals(slot));

            // Include slot if it's valid
            if (!isPastTime && !isBlockedByPTO && !hasAppointment) {
                availableSlots.add(slot);
            }
            startTime = startTime.plusMinutes(30);
        }

        return availableSlots;
    }

    public Optional<Appointment> findById(Integer appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    public List<Appointment> getAppointmentsByPatientId(Integer patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
}