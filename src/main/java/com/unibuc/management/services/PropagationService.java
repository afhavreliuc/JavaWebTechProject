package com.unibuc.management.services;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
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
            try {
                String dwUrl = dwJdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
                System.out.println("DW DataSource URL: " + dwUrl);
            } catch (Exception e) {
                System.err.println("Error checking DW DataSource: " + e.getMessage());
            }
            
            try {
                dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
            } catch (Exception e) {
                try {
                    dwJdbcTemplate.execute("DROP TABLE PatientDW");
                    dwJdbcTemplate.execute("CREATE TABLE PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
                } catch (Exception ex) {}
            }
            
            try {
                dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
            } catch (Exception e) {
                try {
                    dwJdbcTemplate.execute("DROP TABLE DoctorDW");
                    dwJdbcTemplate.execute("CREATE TABLE DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
                } catch (Exception ex) {}
            }
            
            try {
                dwJdbcTemplate.execute("DROP TABLE IF EXISTS AppointmentDW");
                dwJdbcTemplate.execute("CREATE TABLE AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100))");
            } catch (Exception e) {
                System.err.println("Error recreating AppointmentDW: " + e.getMessage());
            }

            try {
                dwJdbcTemplate.execute("DELETE FROM PatientDW");
            } catch (Exception e) {}
            try {
                dwJdbcTemplate.execute("DELETE FROM DoctorDW");
            } catch (Exception e) {}
            try {
                dwJdbcTemplate.execute("DELETE FROM AppointmentDW");
            } catch (Exception e) {}

            System.out.println("Copying patients...");
            List<Map<String, Object>> patients = oltpJdbcTemplate.queryForList("SELECT id, name, medical_record, age FROM patient");
            for (Map<String, Object> patient : patients) {
                dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                        patient.get("ID"), patient.get("NAME"), patient.get("MEDICAL_RECORD"), patient.get("AGE"));
            }
            System.out.println("Copied " + patients.size() + " patients");

            System.out.println("Copying doctors...");
            List<Map<String, Object>> doctors = oltpJdbcTemplate.queryForList("SELECT id, office, number_ofptodays FROM doctor");
            for (Map<String, Object> doctor : doctors) {
                dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                        doctor.get("ID"), doctor.get("OFFICE"), doctor.get("NUMBER_OFPTODAYS"));
            }
            System.out.println("Copied " + doctors.size() + " doctors");

            System.out.println("Copying appointments with financial data...");
            String query = "SELECT a.id, a.status, a.appointment_from, COALESCE(p.amount, 0) as revenue, d.id as doctor_id, d.name as doctor_name " +
                           "FROM appointment a " +
                           "LEFT JOIN payment p ON a.id = p.appointment_id " +
                           "LEFT JOIN doctor d ON a.id_medical_service = d.id_medical_service";
            
            List<Map<String, Object>> appts = oltpJdbcTemplate.queryForList(query);
            for (Map<String, Object> appt : appts) {
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from, revenue, doctor_id, doctor_name) VALUES (?, ?, ?, ?, ?, ?)",
                        appt.get("ID"), 
                        appt.get("STATUS"), 
                        appt.get("APPOINTMENT_FROM"),
                        appt.get("REVENUE"),
                        appt.get("DOCTOR_ID"),
                        appt.get("DOCTOR_NAME"));
            }
            System.out.println("Copied " + appts.size() + " appointments to DW");
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
    
    public void propagatePatientToDW(Patient patient) {
        try {
            ensureTableExists("PatientDW", 
                "CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
            
            dwJdbcTemplate.update("DELETE FROM PatientDW WHERE id = ?", patient.getId());
            int rowsAffected = dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                patient.getId(), patient.getName(), patient.getMedicalRecord(), patient.getAge());
            System.out.println("[DW] Patient " + patient.getId() + " propagated successfully (rows: " + rowsAffected + ")");
        } catch (Exception e) {
            System.err.println("[DW] Error propagating patient to DW: " + e.getMessage());
            e.printStackTrace();
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
                "CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100))");
            
            String sql = "SELECT a.id, a.status, a.appointment_from, COALESCE(p.amount, 0) as revenue, d.id as doctor_id, d.name as doctor_name " +
                         "FROM appointment a " +
                         "LEFT JOIN payment p ON a.id = p.appointment_id " +
                         "LEFT JOIN doctor d ON a.id_medical_service = d.id_medical_service " +
                         "WHERE a.id = ?";
            
            List<Map<String, Object>> results = oltpJdbcTemplate.queryForList(sql, appointment.getId());
            
            if (!results.isEmpty()) {
                Map<String, Object> data = results.get(0);
            dwJdbcTemplate.update("DELETE FROM AppointmentDW WHERE id = ?", appointment.getId());
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from, revenue, doctor_id, doctor_name) VALUES (?, ?, ?, ?, ?, ?)",
                    data.get("ID"), 
                    data.get("STATUS"), 
                    data.get("APPOINTMENT_FROM"),
                    data.get("REVENUE"),
                    data.get("DOCTOR_ID"),
                    data.get("DOCTOR_NAME"));
                System.out.println("[DW] Appointment " + appointment.getId() + " propagated successfully");
            }
        } catch (Exception e) {
            System.err.println("[DW] Error propagating appointment to DW: " + e.getMessage());
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
            try {
                dwJdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            } catch (Exception e) {
                System.out.println("[DW] Creating table " + tableName);
                dwJdbcTemplate.execute(createTableSQL);
                System.out.println("[DW] Table " + tableName + " created successfully");
            }
        } catch (Exception e) {
            System.err.println("[DW] Error ensuring table " + tableName + " exists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getFinancialEvolution() {
        String sql = "SELECT MONTH(appointment_from) as month_num, " +
                     "SUM(CASE WHEN YEAR(appointment_from) = YEAR(CURRENT_DATE()) THEN revenue ELSE 0 END) as current_year, " +
                     "SUM(CASE WHEN YEAR(appointment_from) = YEAR(CURRENT_DATE()) - 1 THEN revenue ELSE 0 END) as previous_year " +
                     "FROM AppointmentDW " +
                     "WHERE YEAR(appointment_from) >= YEAR(CURRENT_DATE()) - 1 " +
                     "GROUP BY MONTH(appointment_from) " +
                     "ORDER BY month_num";
        return dwJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTopDoctors() {
        String sql = "SELECT doctor_id, doctor_name, SUM(revenue) as total_revenue, COUNT(*) as appointment_count " +
                     "FROM AppointmentDW " +
                     "WHERE QUARTER(appointment_from) = QUARTER(CURRENT_DATE()) " +
                     "AND YEAR(appointment_from) = YEAR(CURRENT_DATE()) " +
                     "AND doctor_id IS NOT NULL " +
                     "GROUP BY doctor_id, doctor_name " +
                     "ORDER BY total_revenue DESC " +
                     "FETCH FIRST 5 ROWS ONLY";
        return dwJdbcTemplate.queryForList(sql);
    }
}

