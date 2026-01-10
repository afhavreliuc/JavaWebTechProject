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
    private final PaymentTypeService paymentTypeService;
    private final DoctorService doctorService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 MedicalServiceService medicalServiceService,
                                 PaymentTypeService paymentTypeService,
                                 DoctorService doctorService
                                 ) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.medicalServiceService = medicalServiceService;
        this.paymentTypeService = paymentTypeService;
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestParam Integer patientId,
                                                         @RequestParam Integer medicalServiceId,
                                                         @RequestParam OffsetDateTime appointmentFrom) {

        Optional<Patient> patientOpt = patientService.getPatientById(patientId);
        Optional<MedicalService> medicalServiceOpt = medicalServiceService.getMedicalServiceById(medicalServiceId);
        Appointment appointment = new Appointment();
        appointment.setIsDoctor(false);
        if (patientOpt.isPresent() && medicalServiceOpt.isPresent()) {
            Patient patient = patientOpt.get();
            MedicalService medicalService = medicalServiceOpt.get();

            Optional<Doctor> doctorOpt = doctorService.getDoctorByMedicalService(medicalServiceId);
            if (doctorOpt.isPresent()) {
                appointment.setIsDoctor(true);
            }

            Optional<PaymentType> paymentTypeOpt = paymentTypeService.getPriceForPatient(medicalServiceId, patientId);
            if (paymentTypeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            appointment.setPatient(patient);
            appointment.setMedicalService(medicalService);
            appointment.setPayment(paymentTypeOpt.get());
            appointment.setAppointmentFrom(appointmentFrom);
            appointment.setStatus("Appointed");


            List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(medicalService,
                    appointment.getAppointmentFrom().toLocalDate().toString());

            if (!availableSlots.contains(appointment.getAppointmentFrom())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Requested appointment time is not available.");
            }

            Appointment savedAppointment = appointmentService.createAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);

        } else {
            return ResponseEntity.notFound().build();
        }
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

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer appointmentId) {
        if (appointmentService.findById(appointmentId).isPresent()) {
            appointmentService.deleteAppointment(appointmentId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<?> updateAppointment(@PathVariable Integer appointmentId,
                                               @RequestParam String appointmentFrom) {
        try {
            OffsetDateTime newTime = OffsetDateTime.parse(appointmentFrom);
            Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();

                List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                        appointment.getMedicalService(),
                        newTime.toLocalDate().toString()
                );

                boolean isCurrentSlot = appointment.getAppointmentFrom().toInstant().equals(newTime.toInstant());
                boolean isAvailable = availableSlots.stream()
                        .anyMatch(slot -> slot.toInstant().equals(newTime.toInstant()));

                if (!isAvailable && !isCurrentSlot) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Intervalul orar selectat nu este disponibil.");
                }

                appointment.setAppointmentFrom(newTime);
                Appointment updated = appointmentService.save(appointment);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format dată invalid sau eroare la procesare: " + e.getMessage());
        }
    }
}