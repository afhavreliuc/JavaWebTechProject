package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedicalService", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private MedicalService medicalService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPatient", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    @JoinColumn(name = "idPayment", nullable = false)
    private PaymentType payment;

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

    public PaymentType getPayment() {
        return payment;
    }

    public void setPayment(final PaymentType payment) {
        this.payment = payment;
    }

}
