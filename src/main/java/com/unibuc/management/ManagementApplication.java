package com.unibuc.management;

import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.PatientRepository;
import com.unibuc.management.services.PropagationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = "com.unibuc.management")
public class ManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManagementApplication.class, args);
	}

	@Bean
	CommandLineRunner seedMedicalServices(MedicalServiceRepository medicalServiceRepository) {
		return args -> {
			if (medicalServiceRepository.count() > 0) {
				return;
			}

			MedicalService consultatie = new MedicalService();
			consultatie.setName("Consultație medicină generală");
			consultatie.setSpecialization("Medicină generală");
			consultatie.setStartHour(8);
			consultatie.setEndHour(16);
			consultatie.setPrice(150.0);
			consultatie.setRating(5.0);
			consultatie.setNrOfRatings(0);

			MedicalService cardiologie = new MedicalService();
			cardiologie.setName("Consultație cardiologie");
			cardiologie.setSpecialization("Cardiologie");
			cardiologie.setStartHour(9);
			cardiologie.setEndHour(17);
			cardiologie.setPrice(250.0);
			cardiologie.setRating(5.0);
			cardiologie.setNrOfRatings(0);

			MedicalService pediatrie = new MedicalService();
			pediatrie.setName("Consultație pediatrie");
			pediatrie.setSpecialization("Pediatrie");
			pediatrie.setStartHour(10);
			pediatrie.setEndHour(18);
			pediatrie.setPrice(180.0);
			pediatrie.setRating(5.0);
			pediatrie.setNrOfRatings(0);

			medicalServiceRepository.saveAll(List.of(consultatie, cardiologie, pediatrie));
		};
	}

	@Bean
	CommandLineRunner seedInsuranceProviders(InsuranceProviderRepository insuranceProviderRepository) {
		return args -> {
			if (insuranceProviderRepository.count() > 0) {
				return;
			}

			InsuranceProvider casaDeAsigurari = new InsuranceProvider();
			casaDeAsigurari.setName("Casa de Asigurări de Sănătate");
			casaDeAsigurari.setContactNumber("021-123-4567");

			InsuranceProvider allianz = new InsuranceProvider();
			allianz.setName("Allianz Țiriac");
			allianz.setContactNumber("021-234-5678");

			InsuranceProvider groupama = new InsuranceProvider();
			groupama.setName("Groupama Asigurări");
			groupama.setContactNumber("021-345-6789");

			InsuranceProvider omniasig = new InsuranceProvider();
			omniasig.setName("Omniasig");
			omniasig.setContactNumber("021-456-7890");

			insuranceProviderRepository.saveAll(List.of(casaDeAsigurari, allianz, groupama, omniasig));
		};
	}

	@Bean
	CommandLineRunner initializeDWTables(@Qualifier("dwJdbcTemplate") JdbcTemplate dwJdbcTemplate) {
		return args -> {
			System.out.println("[Startup] Initializing DW tables...");
			try {
				try {
					dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS PatientDW (id INT PRIMARY KEY, name VARCHAR(100), medical_record VARCHAR(4000), age DATE)");
					System.out.println("[Startup] PatientDW table ready");
				} catch (Exception e) {
					System.err.println("[Startup] Error creating PatientDW: " + e.getMessage());
				}

				try {
					dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS DoctorDW (id INT PRIMARY KEY, office VARCHAR(50), number_of_ptodays INT)");
					System.out.println("[Startup] DoctorDW table ready");
				} catch (Exception e) {
					System.err.println("[Startup] Error creating DoctorDW: " + e.getMessage());
				}

				try {
					dwJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS AppointmentDW (id INT PRIMARY KEY, status VARCHAR(50), appointment_from TIMESTAMP, revenue DECIMAL(10,2), doctor_id INT, doctor_name VARCHAR(100))");
					System.out.println("[Startup] AppointmentDW table ready");
				} catch (Exception e) {
					System.err.println("[Startup] Error creating AppointmentDW: " + e.getMessage());
				}

				System.out.println("[Startup] DW tables initialized successfully!");
			} catch (Exception e) {
				System.err.println("[Startup] Error initializing DW tables: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}

	@Bean
	CommandLineRunner syncExistingDataToDW(PropagationService propagationService) {
		return args -> {
			System.out.println("[Startup] Syncing existing data to DW...");
			try {
				propagationService.propagateData();
				System.out.println("[Startup] Existing data sync completed successfully!");
			} catch (Exception e) {
				System.err.println("[Startup] Error during initial sync: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}
}
