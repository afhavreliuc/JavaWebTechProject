package com.unibuc.management.repositories;
import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("""
    SELECT a 
    FROM Appointment a 
    WHERE a.medicalService.id = :medicalServiceId 
    AND a.appointment_from BETWEEN :start AND :end
""")
    List<Appointment> findByMedicalServiceAndDate(
            @Param("medicalServiceId") Integer medicalServiceId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    List<Appointment> findByPatientId(Integer patientId);
}