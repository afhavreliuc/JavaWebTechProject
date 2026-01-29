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
                dwJdbcTemplate.execute("CREATE TABLE AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100), patient_id INT)");
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
            String query = "SELECT a.id, a.status, a.appointment_from, COALESCE(p.amount, 0) as revenue, a.id_patient as patient_id, " +
                           "(SELECT d.id FROM doctor d WHERE d.id_medical_service = a.id_medical_service AND ROWNUM = 1) as doctor_id, " +
                           "(SELECT d.name FROM doctor d WHERE d.id_medical_service = a.id_medical_service AND ROWNUM = 1) as doctor_name " +
                           "FROM appointment a " +
                           "LEFT JOIN payment p ON a.id = p.appointment_id";
            
            List<Map<String, Object>> appts = oltpJdbcTemplate.queryForList(query);
            System.out.println("Found " + appts.size() + " appointments in OLTP");
            int successCount = 0;
            int errorCount = 0;
            for (Map<String, Object> appt : appts) {
                try {
                    Object doctorId = appt.get("DOCTOR_ID");
                    Object doctorName = appt.get("DOCTOR_NAME");
                    
                    if (doctorId == null) {
                        System.out.println("[DW] Warning: Appointment " + appt.get("ID") + " has no associated doctor");
                    }
                    
                    dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from, revenue, doctor_id, doctor_name, patient_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            appt.get("ID"), 
                            appt.get("STATUS"), 
                            appt.get("APPOINTMENT_FROM"),
                            appt.get("REVENUE"),
                            doctorId,
                            doctorName,
                            appt.get("PATIENT_ID"));
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("[DW] Error inserting appointment " + appt.get("ID") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Copied " + successCount + " out of " + appts.size() + " appointments to DW (errors: " + errorCount + ")");
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
                "CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100), patient_id INT)");
            
            String sql = "SELECT a.id, a.status, a.appointment_from, COALESCE(p.amount, 0) as revenue, a.id_patient as patient_id, " +
                         "(SELECT d.id FROM doctor d WHERE d.id_medical_service = a.id_medical_service AND ROWNUM = 1) as doctor_id, " +
                         "(SELECT d.name FROM doctor d WHERE d.id_medical_service = a.id_medical_service AND ROWNUM = 1) as doctor_name " +
                         "FROM appointment a " +
                         "LEFT JOIN payment p ON a.id = p.appointment_id " +
                         "WHERE a.id = ?";
            
            List<Map<String, Object>> results = oltpJdbcTemplate.queryForList(sql, appointment.getId());
            
            if (!results.isEmpty()) {
                Map<String, Object> data = results.get(0);
                dwJdbcTemplate.update("DELETE FROM AppointmentDW WHERE id = ?", appointment.getId());
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from, revenue, doctor_id, doctor_name, patient_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    data.get("ID"), 
                    data.get("STATUS"), 
                    data.get("APPOINTMENT_FROM"),
                    data.get("REVENUE"),
                    data.get("DOCTOR_ID"),
                    data.get("DOCTOR_NAME"),
                    data.get("PATIENT_ID"));
                System.out.println("[DW] Appointment " + appointment.getId() + " propagated successfully");
            }
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

    @Transactional
    public void seedMockData() {
        try {
            System.out.println("Seeding mock data from Word document...");
            
            ensureTableExists("PatientDW", "CREATE TABLE PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
            ensureTableExists("DoctorDW", "CREATE TABLE DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
            ensureTableExists("AppointmentDW", "CREATE TABLE AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100), patient_id INT)");

            dwJdbcTemplate.execute("DELETE FROM AppointmentDW");
            dwJdbcTemplate.execute("DELETE FROM DoctorDW");
            dwJdbcTemplate.execute("DELETE FROM PatientDW");

            Object[][] doctors = {
                {1, "A101", 20, "Dr. Ionescu", "Cardiologie"},
                {2, "A102", 18, "Dr. Popa", "Consultație Generală"},
                {3, "A103", 22, "Dr. Marin", "Pediatrie"},
                {4, "A104", 16, "Dr. Stan", "Neurologie"},
                {5, "B201", 20, "Dr. Radu", "Dermatologie"},
                {6, "B202", 15, "Dr. Dumitrescu", "Ortopedie"},
                {7, "B203", 18, "Dr. Enache", "Ginecologie"},
                {8, "C301", 21, "Dr. Matei", "Oftalmologie"},
                {9, "C302", 19, "Dr. Vasilescu", "ORL"},
                {10, "C303", 17, "Dr. Petre", "Endocrinologie"}
            };

            for (Object[] doc : doctors) {
                dwJdbcTemplate.update("INSERT INTO DoctorDW (id, office, number_of_ptodays) VALUES (?, ?, ?)",
                        doc[0], doc[1], doc[2]);
            }

            Object[][] patients = {
                {1, "Ionescu Ana", "Alergie la penicilină", "1990-06-15"},
                {2, "Popescu Mihai", "Hipertensiune", "1979-03-10"},
                {3, "Marin Elena", null, "1996-09-22"},
                {4, "Stan George", "Diabet tip II", "1971-01-05"},
                {5, "Radu Andreea", null, "1985-11-30"},
                {6, "Dumitru Ion", "Afectiuni cardiace", "1963-04-18"},
                {7, "Iliescu Maria", null, "1999-07-09"},
                {8, "Georgescu Paul", "Migrene frecvente", "1977-02-14"},
                {9, "Munteanu Laura", null, "1991-08-21"},
                {10, "Badea Nicolae", "Probleme articulare", "1966-12-03"}
            };

            for (Object[] pat : patients) {
                dwJdbcTemplate.update("INSERT INTO PatientDW (id, name, medical_record, age) VALUES (?, ?, ?, ?)",
                        pat[0], pat[1], pat[2], java.sql.Date.valueOf((String)pat[3]));
            }

            Object[][] appointments = {
                {1, "COMPLETED", "2024-11-10 09:00:00", 150.00, 2, "Dr. Popa", 1},
                {101, "COMPLETED", "2024-12-10 09:00:00", 150.00, 2, "Dr. Popa", 1},
                {2, "COMPLETED", "2024-11-15 11:30:00", 250.00, 1, "Dr. Ionescu", 2},
                {102, "COMPLETED", "2024-12-20 11:30:00", 250.00, 1, "Dr. Ionescu", 2},
                {3, "COMPLETED", "2024-12-03 10:00:00", 200.00, 5, "Dr. Radu", 3},
                {103, "COMPLETED", "2025-01-05 10:00:00", 200.00, 5, "Dr. Radu", 3},
                {4, "COMPLETED", "2024-12-18 14:00:00", 300.00, 4, "Dr. Stan", 4},
                {104, "COMPLETED", "2025-01-20 14:00:00", 300.00, 4, "Dr. Stan", 4},
                {10, "COMPLETED", "2024-10-05 08:30:00", 150.00, 2, "Dr. Popa", 7},
                {110, "COMPLETED", "2024-11-15 08:30:00", 150.00, 2, "Dr. Popa", 7},
                {11, "COMPLETED", "2024-10-18 12:00:00", 220.00, 6, "Dr. Dumitrescu", 10},
                {111, "COMPLETED", "2024-11-25 12:00:00", 220.00, 6, "Dr. Dumitrescu", 10},
                {201, "COMPLETED", "2023-10-10 10:00:00", 180.00, 3, "Dr. Marin", 5},
                {202, "COMPLETED", "2023-11-12 11:00:00", 210.00, 8, "Dr. Matei", 6},
                {203, "COMPLETED", "2023-12-15 09:00:00", 170.00, 9, "Dr. Vasilescu", 8}
            };

            for (Object[] appt : appointments) {
                dwJdbcTemplate.update("INSERT INTO AppointmentDW (id, status, appointment_from, revenue, doctor_id, doctor_name, patient_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        appt[0], appt[1], java.sql.Timestamp.valueOf((String)appt[2]), appt[3], appt[4], appt[5], appt[6]);
            }

            System.out.println("Mock data seeded successfully!");
        } catch (Exception e) {
            System.err.println("Error seeding mock data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Eroare la popularea cu date mock: " + e.getMessage());
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
                     "SUM(CASE WHEN YEAR(appointment_from) = 2024 THEN revenue ELSE 0 END) as current_year, " +
                     "SUM(CASE WHEN YEAR(appointment_from) = 2023 THEN revenue ELSE 0 END) as previous_year " +
                     "FROM AppointmentDW " +
                     "WHERE YEAR(appointment_from) >= 2023 " +
                     "GROUP BY MONTH(appointment_from) " +
                     "ORDER BY month_num";
        return dwJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTopDoctors() {
        String sql = "SELECT doctor_id, doctor_name, SUM(revenue) as total_revenue, COUNT(*) as appointment_count " +
                     "FROM AppointmentDW " +
                     "WHERE doctor_id IS NOT NULL " +
                     "GROUP BY doctor_id, doctor_name " +
                     "ORDER BY total_revenue DESC " +
                     "FETCH FIRST 5 ROWS ONLY";
        return dwJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getParetoAnalysis() {
        String sql = "SELECT " +
                     "doctor_name, " +
                     "SUM(revenue) AS Venit_Medic, " +
                     "SUM(SUM(revenue)) OVER () AS Venit_Total_Clinica, " +
                     "ROUND((SUM(revenue) * 100.0 / SUM(SUM(revenue)) OVER ()), 2) AS Procent_Din_Total, " +
                     "SUM(SUM(revenue)) OVER (ORDER BY SUM(revenue) DESC ROWS UNBOUNDED PRECEDING) AS Venit_Cumulat " +
                     "FROM AppointmentDW " +
                     "WHERE doctor_id IS NOT NULL AND doctor_name IS NOT NULL " +
                     "GROUP BY doctor_name " +
                     "ORDER BY Venit_Medic DESC";
        return dwJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getPatientRecurrence() {
        try {
            String sql = "SELECT " +
                         "p.name AS Pacient, " +
                         "a.appointment_from AS Data_Vizita_Curenta, " +
                         "LAG(a.appointment_from, 1) OVER (PARTITION BY p.id ORDER BY a.appointment_from) AS Data_Vizita_Anterioara, " +
                         "DATEDIFF('DAY', LAG(a.appointment_from, 1) OVER (PARTITION BY p.id ORDER BY a.appointment_from), a.appointment_from) AS Zile_Intre_Vizite " +
                         "FROM AppointmentDW a " +
                         "JOIN PatientDW p ON a.patient_id = p.id " +
                         "ORDER BY p.name, a.appointment_from";
            return dwJdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("Error in getPatientRecurrence: " + e.getMessage());
            return List.of();
        }
    }
}

