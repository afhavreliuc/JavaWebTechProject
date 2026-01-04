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

    @Column(columnDefinition = "text")
    private String medicalRecord;

    @Column(nullable = false)
    private Boolean insurance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subscription_plan")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patients", "services"})
    private SubscriptionPlan activeSubscription;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Boolean sex;

    @OneToMany(mappedBy = "patient")
    private Set<Appointment> patientAppointments;

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

    public Boolean getInsurance() {
        return insurance;
    }

    public void setInsurance(final Boolean insurance) {
        this.insurance = insurance;
    }

    public SubscriptionPlan getActiveSubscription() {
        return activeSubscription;
    }

    public void setActiveSubscription(SubscriptionPlan activeSubscription) {
        this.activeSubscription = activeSubscription;
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

}
