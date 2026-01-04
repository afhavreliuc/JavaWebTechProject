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

    @Transactional
    public void propagateData() {
        // 1. Create tables in DW if they don't exist (Simplified versions)
        dwJdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PatientDW' AND xtype='U') " +
                "CREATE TABLE PatientDW (id INT PRIMARY KEY, name NVARCHAR(100), medical_record NVARCHAR(MAX), age INT)");

        dwJdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='DoctorDW' AND xtype='U') " +
                "CREATE TABLE DoctorDW (id INT PRIMARY KEY, office NVARCHAR(50), number_of_ptodays INT)");

        dwJdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='AppointmentDW' AND xtype='U') " +
                "CREATE TABLE AppointmentDW (id INT PRIMARY KEY, status NVARCHAR(50), appointment_from DATETIMEOFFSET)");

        // 2. Clear DW tables for a fresh sync (Simplified ETL)
        dwJdbcTemplate.execute("DELETE FROM PatientDW");
        dwJdbcTemplate.execute("DELETE FROM DoctorDW");
        dwJdbcTemplate.execute("DELETE FROM AppointmentDW");

        // 3. Copy Patients
        List<Map<String, Object>> patients = oltpJdbcTemplate.queryForList("SELECT id, name, medical_record, age FROM Patient");
        for (Map<String, Object> patient : patients) {
            dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                    patient.get("id"), patient.get("name"), patient.get("medical_record"), patient.get("age"));
        }

        // 4. Copy Doctors
        List<Map<String, Object>> doctors = oltpJdbcTemplate.queryForList("SELECT id, office, number_ofpto_days FROM Doctor");
        for (Map<String, Object> doctor : doctors) {
            dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                    doctor.get("id"), doctor.get("office"), doctor.get("number_ofpto_days"));
        }

        // 5. Copy Appointments
        List<Map<String, Object>> appts = oltpJdbcTemplate.queryForList("SELECT id, status, appointment_from FROM Appointment");
        for (Map<String, Object> appt : appts) {
            dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from) VALUES (?, ?, ?)",
                    appt.get("id"), appt.get("status"), appt.get("appointment_from"));
        }
    }

    public Map<String, Object> getSyncStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("oltp_patients", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM Patient", Integer.class));
            stats.put("dw_patients", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM PatientDW", Integer.class));
            
            stats.put("oltp_doctors", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM Doctor", Integer.class));
            stats.put("dw_doctors", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM DoctorDW", Integer.class));
            
            stats.put("oltp_appointments", oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM Appointment", Integer.class));
            stats.put("dw_appointments", dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM AppointmentDW", Integer.class));
        } catch (Exception e) {
            // Tables might not exist yet
            stats.put("error", "Unele tabele DW nu au fost create încă. Rulează o sincronizare.");
        }
        
        return stats;
    }
}

