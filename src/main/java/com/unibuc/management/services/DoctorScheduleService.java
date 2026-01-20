package com.unibuc.management.services;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.PaidTimeOff;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class DoctorScheduleService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleService(AppointmentRepository appointmentRepository, PaidTimeOffRepository ptoRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.ptoRepository = ptoRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<ScheduleEntry> getDoctorScheduleForDay(Integer doctorId, LocalDate date) {
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<Appointment> appointments = appointmentRepository.findByMedicalServiceAndDate(doctorId, startOfDay, endOfDay);

        List<PaidTimeOff> ptoEntries = ptoRepository.findByDoctorAndDate(doctorId, startOfDay, endOfDay);

        List<ScheduleEntry> schedule = new ArrayList<>();

        schedule.addAll(appointments.stream()
                .map(appointment -> new ScheduleEntry("Appointment", appointment.getAppointmentFrom(), appointment.getAppointmentFrom(), appointment))
                .collect(Collectors.toList()));

        schedule.addAll(ptoEntries.stream()
                .map(pto -> new ScheduleEntry("Paid Time Off", pto.getPtoFrom(), pto.getPtoTo(), pto))
                .collect(Collectors.toList()));

        return schedule;
    }

    public String schedulePTO(Integer doctorId, OffsetDateTime start, OffsetDateTime end) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);

        if (doctorOpt.isEmpty()) {
            return "Doctor with ID " + doctorId + " not found.";
        }

        Doctor doctor = doctorOpt.get();
        List<Appointment> appointments = appointmentRepository.findByMedicalServiceAndDate(doctor.getMedicalService().getId(), start, end);

        if (!appointments.isEmpty()) {
            return "Doctor has appointments on the selected day. PTO cannot be scheduled.";
        }

        // Create and save the new PTO entry
        PaidTimeOff pto = new PaidTimeOff();
        pto.setDoctor(doctor);  // Use the existing doctor entity
        pto.setPtoFrom(start);
        pto.setPtoTo(end);

        ptoRepository.save(pto);

        return "PTO successfully scheduled!";
    }
}