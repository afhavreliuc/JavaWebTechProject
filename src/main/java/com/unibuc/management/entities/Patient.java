package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.Set;


@Entity
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "medical_record", length = 4000)
    private String medicalRecord;

    @Column(nullable = false)
    private Boolean subscription;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Boolean sex;

    @OneToMany(mappedBy = "patient")
    private Set<Appointment> patientAppointments;

    @ManyToOne
    @JoinColumn(name = "insurance_provider_id")
    private InsuranceProvider insuranceProvider;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(final String medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public Boolean getSubscription() {
        return subscription;
    }

    public void setSubscription(final Boolean subscription) {
        this.subscription = subscription;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(final Boolean sex) {
        this.sex = sex;
    }

    public Set<Appointment> getPatientAppointments() {
        return patientAppointments;
    }

    public void setPatientAppointments(final Set<Appointment> patientAppointments) {
        this.patientAppointments = patientAppointments;
    }

    public InsuranceProvider getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(InsuranceProvider insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

}
