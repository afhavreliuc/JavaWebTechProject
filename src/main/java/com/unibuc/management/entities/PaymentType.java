package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Set;


@Entity
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PaymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @Column(nullable = false)
    private Boolean isDoctor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean withInsurance;

    @Column(nullable = false)
    private Boolean withSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "idMedicalService", nullable = false)
    private MedicalService medicalService;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public Boolean getWithInsurance() {
        return withInsurance;
    }

    public void setWithInsurance(final Boolean withInsurance) {
        this.withInsurance = withInsurance;
    }

    public Boolean getWithSubscription() {
        return withSubscription;
    }

    public void setWithSubscription(final Boolean withSubscription) {
        this.withSubscription = withSubscription;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(final MedicalService medicalService) {
        this.medicalService = medicalService;
    }

}
