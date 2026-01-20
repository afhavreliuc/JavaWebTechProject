package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(nullable = false)
    private Integer startHour;

    @Column(nullable = false)
    private Integer endHour;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Integer nrOfRatings;

    @OneToMany(mappedBy = "medicalService")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Set<Doctor> medicalServiceDoctors;

    @OneToMany(mappedBy = "medicalService")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Set<Appointment> medicalServiceAppointments;

    @ManyToMany
    @JoinTable(
            name = "service_insurance_coverage",
            joinColumns = @JoinColumn(name = "id_medical_service"),
            inverseJoinColumns = @JoinColumn(name = "id_insurance_provider")
    )
    private Set<InsuranceProvider> coveredByInsurances;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(final String specialization) {
        this.specialization = specialization;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(final Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(final Integer endHour) {
        this.endHour = endHour;
    }
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    public Double getRating() {
        return rating;
    }

    public void setRating(final Double rating) {
        this.rating = rating;
    }

    public Integer getNrOfRatings() {
        return nrOfRatings;
    }

    public void setNrOfRatings(final Integer nrOfRatings) {
        this.nrOfRatings = nrOfRatings;
    }

    public Set<Doctor> getMedicalServiceDoctors() {
        return medicalServiceDoctors;
    }

    public void setMedicalServiceDoctors(final Set<Doctor> medicalServiceDoctors) {
        this.medicalServiceDoctors = medicalServiceDoctors;
    }

    public Set<Appointment> getMedicalServiceAppointments() {
        return medicalServiceAppointments;
    }

    public void setMedicalServiceAppointments(final Set<Appointment> medicalServiceAppointments) {
        this.medicalServiceAppointments = medicalServiceAppointments;
    }

    public Set<InsuranceProvider> getCoveredByInsurances() {
        return coveredByInsurances;
    }

    public void setCoveredByInsurances(Set<InsuranceProvider> coveredByInsurances) {
        this.coveredByInsurances = coveredByInsurances;
    }
}
