package com.unibuc.management.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PropagationService {

    @Autowired
    private JdbcTemplate oltpJdbcTemplate;

    @Autowired
    @Qualifier("dwJdbcTemplate")
    private JdbcTemplate dwJdbcTemplate;

    public void propagateData() {
        try {
            System.out.println("Starting ETL process...");
            // 1. Create tables in DW if they don't exist
            dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(MAX), age INT)");
            dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
            dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP WITH TIME ZONE)");

            // 2. Clear DW tables for a fresh sync
            dwJdbcTemplate.execute("DELETE FROM PatientDW");
            dwJdbcTemplate.execute("DELETE FROM DoctorDW");
            dwJdbcTemplate.execute("DELETE FROM AppointmentDW");

            // 3. Copy Patients
            System.out.println("Copying patients...");
            List<Map<String, Object>> patients = oltpJdbcTemplate.queryForList("SELECT id, name, medical_record, age FROM patient");
            for (Map<String, Object> patient : patients) {
                dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                        patient.get("ID"), patient.get("NAME"), patient.get("MEDICAL_RECORD"), patient.get("AGE"));
            }

            // 4. Copy Doctors
            System.out.println("Copying doctors...");
            List<Map<String, Object>> doctors = oltpJdbcTemplate.queryForList("SELECT id, office, number_ofptodays FROM doctor");
            for (Map<String, Object> doctor : doctors) {
                dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                        doctor.get("ID"), doctor.get("OFFICE"), doctor.get("NUMBER_OFPTODAYS"));
            }

            // 5. Copy Appointments
            System.out.println("Copying appointments...");
            List<Map<String, Object>> appts = oltpJdbcTemplate.queryForList("SELECT id, status, appointment_from FROM appointment");
            for (Map<String, Object> appt : appts) {
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from) VALUES (?, ?, ?)",
                        appt.get("ID"), appt.get("STATUS"), appt.get("APPOINTMENT_FROM"));
            }
            System.out.println("ETL process completed successfully!");
        } catch (Exception e) {
            System.err.println("ETL Error during sync: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Eroare la procesul ETL: " + e.getMessage());
        }
    }

    public Map<String, Object> getSyncStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("oltp_patients", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM patient", Integer.class));
            stats.put("dw_patients", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM PatientDW", Integer.class));
            
            stats.put("oltp_doctors", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM doctor", Integer.class));
            stats.put("dw_doctors", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM DoctorDW", Integer.class));
            
            stats.put("oltp_appointments", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment", Integer.class));
            stats.put("dw_appointments", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM AppointmentDW", Integer.class));
        } catch (Exception e) {
            // Tables might not exist yet
            stats.put("error", "Unele tabele DW nu au fost create încă. Rulează o sincronizare.");
        }
        
        return stats;
    }
}

