package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @Column(nullable = false)
    private Boolean isDoctor;

    @Column(nullable = false)
    private OffsetDateTime appointment_from;

    @Column(nullable = false, length = 50)
    private String status;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedicalService", nullable = false)
    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private MedicalService medicalService;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idPatient", nullable = false)
    @JsonBackReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Patient patient;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Boolean getIsDoctor() {
        return isDoctor;
    }

    public void setIsDoctor(final Boolean isDoctor) {
        this.isDoctor = isDoctor;
    }

    public OffsetDateTime getAppointmentFrom() {
        return appointment_from;
    }

    public void setAppointmentFrom(final OffsetDateTime appointmentFrom) {
        this.appointment_from = appointmentFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(final MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(final Patient patient) {
        this.patient = patient;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(final Payment payment) {
        this.payment = payment;
    }



}
