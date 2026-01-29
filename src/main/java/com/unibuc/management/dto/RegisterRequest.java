package com.unibuc.management.dto;

import java.time.LocalDate;

public class RegisterRequest {
    private String username;
    private String password;
    private String role;
    private String fullName;
    private LocalDate age;
    private Boolean sex;
    private String office;
    private Integer numberOfPTOdays;
    private Integer medicalServiceId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getAge() {
        return age;
    }

    public void setAge(LocalDate age) {
        this.age = age;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public Integer getNumberOfPTOdays() {
        return numberOfPTOdays;
    }

    public void setNumberOfPTOdays(Integer numberOfPTOdays) {
        this.numberOfPTOdays = numberOfPTOdays;
    }

    public Integer getMedicalServiceId() {
        return medicalServiceId;
    }

    public void setMedicalServiceId(Integer medicalServiceId) {
        this.medicalServiceId = medicalServiceId;
    }
}