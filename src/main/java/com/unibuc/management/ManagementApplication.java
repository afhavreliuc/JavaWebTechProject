package com.unibuc.management;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan(basePackages = "com.unibuc.management") // Ensure your entities are being scanned
public class ManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManagementApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(MedicalServiceRepository repository, com.unibuc.management.repositories.PaymentTypeRepository paymentRepository) {
		return args -> {
			if (repository.count() == 0) {
				MedicalService cardiores = repository.save(createService("Cardiologie", "Cardiologie", 8, 16));
				MedicalService generalres = repository.save(createService("Consultație Generală", "Medicină Internă", 9, 17));
				MedicalService pedres = repository.save(createService("Pediatrie", "Pediatrie", 10, 18));
				MedicalService neurores = repository.save(createService("Neurologie", "Neurologie", 8, 14));

				paymentRepository.save(createPayment(cardiores, 200));
				paymentRepository.save(createPayment(generalres, 100));
				paymentRepository.save(createPayment(pedres, 150));
				paymentRepository.save(createPayment(neurores, 250));

				System.out.println("Baza de date a fost populată cu servicii și prețuri implicite.");
			}
		};
	}

	private com.unibuc.management.entities.PaymentType createPayment(MedicalService service, int price) {
		com.unibuc.management.entities.PaymentType p = new com.unibuc.management.entities.PaymentType();
		p.setMedicalService(service);
		p.setPrice(java.math.BigDecimal.valueOf(price));
		p.setIsDoctor(true);
		p.setWithInsurance(false);
		p.setWithSubscription(false);
		return p;
	}

	private MedicalService createService(String name, String specialization, int start, int end) {
		MedicalService service = new MedicalService();
		service.setName(name);
		service.setSpecialization(specialization);
		service.setStartHour(start);
		service.setEndHour(end);
		service.setRating(5.0);
		service.setNrOfRatings(0);
		return service;
	}
}
