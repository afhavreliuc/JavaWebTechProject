package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Set;


@Entity
@Access(AccessType.FIELD)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private Boolean subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_insurance_provider")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "coveredServices"})
    private InsuranceProvider insuranceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subscription_plan")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patients", "services"})
    private SubscriptionPlan activeSubscription;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Boolean sex;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public Boolean getSubscription() {
        return subscription;
    }

    public void setSubscription(final Boolean subscription) {
        this.subscription = subscription;
    }

    public InsuranceProvider getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(InsuranceProvider insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
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
