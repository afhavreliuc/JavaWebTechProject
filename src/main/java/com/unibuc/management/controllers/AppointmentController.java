package com.unibuc.management.controllers;

import com.unibuc.management.entities.*;
import com.unibuc.management.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final MedicalServiceService medicalServiceService;
    private final PaymentService paymentService;
    private final DoctorService doctorService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 MedicalServiceService medicalServiceService,
                                 PaymentService paymentService,
                                 DoctorService doctorService
                                 ) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.medicalServiceService = medicalServiceService;
        this.paymentService = paymentService;
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Appointment> createAppointment(@RequestParam Integer patientId,
                                                         @RequestParam Integer medicalServiceId,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime appointmentFrom) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);

        if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Patient> patientOpt = patientService.getPatientById(patientId);
        Optional<MedicalService> medicalServiceOpt = medicalServiceService.getMedicalServiceById(medicalServiceId);

        if (patientOpt.isEmpty() || medicalServiceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = patientOpt.get();
        MedicalService medicalService = medicalServiceOpt.get();

        Appointment appointment = new Appointment();
        Optional<Doctor> doctorOpt = doctorService.getDoctorByMedicalService(medicalServiceId);
        appointment.setIsDoctor(doctorOpt.isPresent());

        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(appointmentFrom);
        appointment.setStatus("Appointed");

        Payment payment = paymentService.createPaymentForPatient(patient, medicalService, appointment);
        appointment.setPayment(payment);

        List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                medicalService,
                appointment.getAppointmentFrom().toLocalDate().toString()
        );

        boolean slotAvailable = availableSlots.stream().anyMatch(slot -> 
            slot.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                .equals(appointment.getAppointmentFrom().truncatedTo(java.time.temporal.ChronoUnit.MINUTES))
        );
        if (!slotAvailable) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Appointment savedAppointment = appointmentService.createAppointment(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }
    @GetMapping("/available-times")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTimeSlots(@RequestParam Integer medicalServiceId,
                                                                      @RequestParam String date) {
        Optional<MedicalService> medicalService = medicalServiceService.getMedicalServiceById(medicalServiceId);
        if (medicalService.isPresent()) {
            List<OffsetDateTime> availableTimeSlots = appointmentService.getAvailableTimeSlots(medicalService.get(), date);
            return ResponseEntity.ok(availableTimeSlots);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable Integer patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            Optional<Doctor> currentDoctorOpt = doctorService.getDoctorByUsername(currentUsername);
            if (currentDoctorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Integer doctorId = currentDoctorOpt.get().getId();
            appointments = appointments.stream()
                    .filter(a -> a.getMedicalService().getMedicalServiceDoctors().stream()
                            .anyMatch(d -> d.getId().equals(doctorId)))
                    .toList();
        }

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Integer doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Doctor> currentDoctorOpt = doctorService.getDoctorByUsername(currentUsername);

        if (currentDoctorOpt.isEmpty() || !currentDoctorOpt.get().getId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Integer id,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime appointmentFrom) {
        Optional<Appointment> appointmentOpt = appointmentService.findById(id);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = appointmentOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isOwner = false;
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isPresent() && currentPatientOpt.get().getId().equals(appointment.getPatient().getId())) {
                isOwner = true;
            }
        } else if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            Optional<Doctor> currentDoctorOpt = doctorService.getDoctorByUsername(currentUsername);
            if (currentDoctorOpt.isPresent()) {
                Integer doctorId = currentDoctorOpt.get().getId();
                if (appointment.getMedicalService().getMedicalServiceDoctors().stream().anyMatch(d -> d.getId().equals(doctorId))) {
                    isOwner = true;
                }
            }
        }

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MedicalService medicalService = appointment.getMedicalService();

        List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                medicalService,
                appointmentFrom.toLocalDate().toString()
        );

        boolean slotAvailable = availableSlots.stream().anyMatch(slot -> 
            slot.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                .equals(appointmentFrom.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))
        );

        if (!slotAvailable) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        appointment.setAppointmentFrom(appointmentFrom);
        Appointment updatedAppointment = appointmentService.save(appointment);
        return ResponseEntity.ok(updatedAppointment);
    }

    @PostMapping("/{appointmentId}/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> submitFeedback(@PathVariable Integer appointmentId,
                                                 @RequestParam float rating) {
        Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Appointment not found or already completed.");
        }

        Appointment appointment = appointmentOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);

        if (currentPatientOpt.isEmpty() || !currentPatientOpt.get().getId().equals(appointment.getPatient().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only submit feedback for your own appointments.");
        }

        OffsetDateTime currentTime = OffsetDateTime.now();
        if (currentTime.isBefore(appointment.getAppointmentFrom().plusMinutes(30))) {
            return ResponseEntity.badRequest().body("Feedback can only be submitted after 30 minutes from the appointment start time.");
        }

        MedicalService service = appointment.getMedicalService();
        double newRating = (service.getRating() * service.getNrOfRatings() + rating) / (service.getNrOfRatings() + 1);
        service.setRating(newRating);
        service.setNrOfRatings(service.getNrOfRatings() + 1);
        medicalServiceService.save(service);

        appointment.setStatus("Completed");
        appointmentService.save(appointment);

        return ResponseEntity.ok("Feedback submitted successfully.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer id) {
        Optional<Appointment> appointmentOpt = appointmentService.findById(id);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appointment = appointmentOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isOwner = false;
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            Optional<Patient> currentPatientOpt = patientService.getPatientByUsername(currentUsername);
            if (currentPatientOpt.isPresent() && currentPatientOpt.get().getId().equals(appointment.getPatient().getId())) {
                isOwner = true;
            }
        } else if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            Optional<Doctor> currentDoctorOpt = doctorService.getDoctorByUsername(currentUsername);
            if (currentDoctorOpt.isPresent()) {
                Integer doctorId = currentDoctorOpt.get().getId();
                if (appointment.getMedicalService().getMedicalServiceDoctors().stream().anyMatch(d -> d.getId().equals(doctorId))) {
                    isOwner = true;
                }
            }
        }

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = appointmentService.deleteAppointment(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}