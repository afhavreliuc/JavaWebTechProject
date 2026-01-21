package com.unibuc.management.services;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.Patient;
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
            // Verify DW connection
            try {
                String dwUrl = dwJdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
                System.out.println("DW DataSource URL: " + dwUrl);
            } catch (Exception e) {
                System.err.println("Error checking DW DataSource: " + e.getMessage());
            }
            
            // 1. Create tables in DW if they don't exist (H2 compatible syntax)
            try {
                dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
            } catch (Exception e) {
                // Table might already exist, try to drop and recreate
                try {
                    dwJdbcTemplate.execute("DROP TABLE PatientDW");
                    dwJdbcTemplate.execute("CREATE TABLE PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
                } catch (Exception ex) {
                    // Ignore if drop fails
                }
            }
            
            try {
                dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
            } catch (Exception e) {
                try {
                    dwJdbcTemplate.execute("DROP TABLE DoctorDW");
                    dwJdbcTemplate.execute("CREATE TABLE DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
                } catch (Exception ex) {
                    // Ignore if drop fails
                }
            }
            
            try {
                dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP)");
            } catch (Exception e) {
                try {
                    dwJdbcTemplate.execute("DROP TABLE AppointmentDW");
                    dwJdbcTemplate.execute("CREATE TABLE AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP)");
                } catch (Exception ex) {
                    // Ignore if drop fails
                }
            }

            // 2. Clear DW tables for a fresh sync
            try {
                dwJdbcTemplate.execute("DELETE FROM PatientDW");
            } catch (Exception e) {
                // Table might not exist yet, ignore
            }
            try {
                dwJdbcTemplate.execute("DELETE FROM DoctorDW");
            } catch (Exception e) {
                // Table might not exist yet, ignore
            }
            try {
                dwJdbcTemplate.execute("DELETE FROM AppointmentDW");
            } catch (Exception e) {
                // Table might not exist yet, ignore
            }

            // 3. Copy Patients
            System.out.println("Copying patients...");
            List<Map<String, Object>> patients = oltpJdbcTemplate.queryForList("SELECT id, name, medical_record, age FROM patient");
            for (Map<String, Object> patient : patients) {
                dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                        patient.get("ID"), patient.get("NAME"), patient.get("MEDICAL_RECORD"), patient.get("AGE"));
            }
            System.out.println("Copied " + patients.size() + " patients");

            // 4. Copy Doctors
            System.out.println("Copying doctors...");
            List<Map<String, Object>> doctors = oltpJdbcTemplate.queryForList("SELECT id, office, number_ofptodays FROM doctor");
            for (Map<String, Object> doctor : doctors) {
                dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                        doctor.get("ID"), doctor.get("OFFICE"), doctor.get("NUMBER_OFPTODAYS"));
            }
            System.out.println("Copied " + doctors.size() + " doctors");

            // 5. Copy Appointments
            System.out.println("Copying appointments...");
            List<Map<String, Object>> appts = oltpJdbcTemplate.queryForList("SELECT id, status, appointment_from FROM appointment");
            for (Map<String, Object> appt : appts) {
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from) VALUES (?, ?, ?)",
                        appt.get("ID"), appt.get("STATUS"), appt.get("APPOINTMENT_FROM"));
            }
            System.out.println("Copied " + appts.size() + " appointments");
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
            Integer oltpPatients = oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM patient", Integer.class);
            stats.put("oltp_patients", oltpPatients);
            System.out.println("[Stats] OLTP Patients: " + oltpPatients);
        } catch (Exception e) {
            stats.put("oltp_patients", 0);
            System.err.println("[Stats] Error counting OLTP patients: " + e.getMessage());
        }
        
        try {
            Integer dwPatients = dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM PatientDW", Integer.class);
            stats.put("dw_patients", dwPatients);
            System.out.println("[Stats] DW Patients: " + dwPatients);
        } catch (Exception e) {
            stats.put("dw_patients", 0);
            System.err.println("[Stats] Error counting DW patients: " + e.getMessage());
        }
        
        try {
            Integer oltpDoctors = oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM doctor", Integer.class);
            stats.put("oltp_doctors", oltpDoctors);
            System.out.println("[Stats] OLTP Doctors: " + oltpDoctors);
        } catch (Exception e) {
            stats.put("oltp_doctors", 0);
            System.err.println("[Stats] Error counting OLTP doctors: " + e.getMessage());
        }
        
        try {
            Integer dwDoctors = dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM DoctorDW", Integer.class);
            stats.put("dw_doctors", dwDoctors);
            System.out.println("[Stats] DW Doctors: " + dwDoctors);
        } catch (Exception e) {
            stats.put("dw_doctors", 0);
            System.err.println("[Stats] Error counting DW doctors: " + e.getMessage());
        }
        
        try {
            Integer oltpAppointments = oltpJdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment", Integer.class);
            stats.put("oltp_appointments", oltpAppointments);
            System.out.println("[Stats] OLTP Appointments: " + oltpAppointments);
        } catch (Exception e) {
            stats.put("oltp_appointments", 0);
            System.err.println("[Stats] Error counting OLTP appointments: " + e.getMessage());
        }
        
        try {
            Integer dwAppointments = dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM AppointmentDW", Integer.class);
            stats.put("dw_appointments", dwAppointments);
            System.out.println("[Stats] DW Appointments: " + dwAppointments);
        } catch (Exception e) {
            stats.put("dw_appointments", 0);
            System.err.println("[Stats] Error counting DW appointments: " + e.getMessage());
        }
        
        // Check if any DW tables don't exist
        boolean tablesMissing = false;
        try {
            dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM PatientDW", Integer.class);
        } catch (Exception e) {
            tablesMissing = true;
        }
        
        if (tablesMissing) {
            stats.put("error", "Unele tabele DW nu au fost create încă. Rulează o sincronizare.");
        }
        
        return stats;
    }

    // Metode pentru propagare automată (folosite de Entity Listeners)
    
    public void propagatePatientToDW(Patient patient) {
        try {
            ensureTableExists("PatientDW", 
                "CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
            
            // H2 doesn't support MERGE, so we use DELETE + INSERT
            dwJdbcTemplate.update("DELETE FROM PatientDW WHERE id = ?", patient.getId());
            int rowsAffected = dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                patient.getId(), patient.getName(), patient.getMedicalRecord(), patient.getAge());
            System.out.println("[DW] Patient " + patient.getId() + " propagated successfully (rows: " + rowsAffected + ")");
        } catch (Exception e) {
            System.err.println("[DW] Error propagating patient to DW: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - don't interrupt OLTP operation
        }
    }

    public void propagateDoctorToDW(Doctor doctor) {
        try {
            ensureTableExists("DoctorDW",
                "CREATE TABLE IF NOT EXISTS DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
            
            dwJdbcTemplate.update("DELETE FROM DoctorDW WHERE id = ?", doctor.getId());
            int rowsAffected = dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                doctor.getId(), doctor.getOffice(), doctor.getNumberOfPtodays());
            System.out.println("[DW] Doctor " + doctor.getId() + " propagated successfully (rows: " + rowsAffected + ")");
        } catch (Exception e) {
            System.err.println("[DW] Error propagating doctor to DW: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void propagateAppointmentToDW(Appointment appointment) {
        try {
            ensureTableExists("AppointmentDW",
                "CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP)");
            
            dwJdbcTemplate.update("DELETE FROM AppointmentDW WHERE id = ?", appointment.getId());
            int rowsAffected = dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from) VALUES (?, ?, ?)",
                appointment.getId(), appointment.getStatus(), appointment.getAppointmentFrom());
            System.out.println("[DW] Appointment " + appointment.getId() + " propagated successfully (rows: " + rowsAffected + ")");
        } catch (Exception e) {
            System.err.println("[DW] Error propagating appointment to DW: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removePatientFromDW(Integer id) {
        try {
            dwJdbcTemplate.update("DELETE FROM PatientDW WHERE id = ?", id);
        } catch (Exception e) {
            System.err.println("Error removing patient from DW: " + e.getMessage());
        }
    }

    public void removeDoctorFromDW(Integer id) {
        try {
            dwJdbcTemplate.update("DELETE FROM DoctorDW WHERE id = ?", id);
        } catch (Exception e) {
            System.err.println("Error removing doctor from DW: " + e.getMessage());
        }
    }

    public void removeAppointmentFromDW(Integer id) {
        try {
            dwJdbcTemplate.update("DELETE FROM AppointmentDW WHERE id = ?", id);
        } catch (Exception e) {
            System.err.println("Error removing appointment from DW: " + e.getMessage());
        }
    }

    private void ensureTableExists(String tableName, String createTableSQL) {
        try {
            // Try to query the table first to see if it exists
            try {
                dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
                // Table exists, no need to create
            } catch (Exception e) {
                // Table doesn't exist, create it
                System.out.println("[DW] Creating table " + tableName);
                dwJdbcTemplate.execute(createTableSQL);
                System.out.println("[DW] Table " + tableName + " created successfully");
            }
        } catch (Exception e) {
            System.err.println("[DW] Error ensuring table " + tableName + " exists: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

