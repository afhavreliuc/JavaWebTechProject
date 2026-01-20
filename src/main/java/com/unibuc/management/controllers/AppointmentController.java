package com.unibuc.management.controllers;

import com.unibuc.management.entities.*;
import com.unibuc.management.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Appointment> createAppointment(@RequestParam Integer patientId,
                                                         @RequestParam Integer medicalServiceId,
                                                         @RequestParam OffsetDateTime appointmentFrom) {

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

        if (!availableSlots.contains(appointment.getAppointmentFrom())) {
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
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable Integer patientId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/{appointmentId}/feedback")
    public ResponseEntity<String> submitFeedback(@PathVariable Integer appointmentId,
                                                 @RequestParam float rating) {
        Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Appointment not found or already completed.");
        }

        Appointment appointment = appointmentOpt.get();
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
}