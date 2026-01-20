package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Set;


@Entity
@Access(AccessType.FIELD)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 50)
    private String office;

    @Column(nullable = false)
    private Integer numberOfPTOdays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedicalService", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MedicalService medicalService;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private Set<PaidTimeOff> doctorPaidTimeOffs;

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

    public String getOffice() {
        return office;
    }

    public void setOffice(final String office) {
        this.office = office;
    }

    public Integer getNumberOfPtodays() {
        return numberOfPTOdays;
    }

    public void setNumberOfPtodays(final Integer numberOfPtodays) {
        this.numberOfPTOdays = numberOfPtodays;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(final MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    public Set<PaidTimeOff> getDoctorPaidTimeOffs() {
        return doctorPaidTimeOffs;
    }

    public void setDoctorPaidTimeOffs(final Set<PaidTimeOff> doctorPaidTimeOffs) {
        this.doctorPaidTimeOffs = doctorPaidTimeOffs;
    }

}
