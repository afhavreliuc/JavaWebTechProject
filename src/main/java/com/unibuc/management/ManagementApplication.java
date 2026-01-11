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
	CommandLineRunner initDatabase(MedicalServiceRepository repository,
								   com.unibuc.management.repositories.PaymentTypeRepository paymentRepository,
								   com.unibuc.management.repositories.InsuranceProviderRepository insuranceRepository) {
		return args -> {
			if (insuranceRepository.count() == 0) {
				insuranceRepository.save(createInsurance("CASMB", "021-302-11-00"));
				insuranceRepository.save(createInsurance("Allianz-Tiriac", "021-201-91-00"));
				insuranceRepository.save(createInsurance("Signal Iduna", "021-301-30-00"));
				insuranceRepository.save(createInsurance("Groupama Asigurari", "0374-110-110"));
				insuranceRepository.save(createInsurance("Omniasig", "021-9669"));
				insuranceRepository.save(createInsurance("Generali", "021-312-36-35"));
				System.out.println("Baza de date a fost populată cu furnizori de asigurări.");
			}

			if (repository.count() == 0) {
				MedicalService cardiores = repository.save(createService("Cardiologie", "Cardiologie", 8, 16));
				MedicalService generalres = repository.save(createService("Consultație Generală", "Medicină Internă", 9, 17));
				MedicalService pedres = repository.save(createService("Pediatrie", "Pediatrie", 10, 18));
				MedicalService neurores = repository.save(createService("Neurologie", "Neurologie", 8, 14));

				paymentRepository.save(createPayment(cardiores, 200, false, false));
				paymentRepository.save(createPayment(cardiores, 150, true, false));
				paymentRepository.save(createPayment(cardiores, 100, false, true));

				paymentRepository.save(createPayment(generalres, 100, false, false));
				paymentRepository.save(createPayment(generalres, 50, true, false));
				paymentRepository.save(createPayment(generalres, 0, false, true));

				paymentRepository.save(createPayment(pedres, 150, false, false));
				paymentRepository.save(createPayment(pedres, 100, true, false));
				paymentRepository.save(createPayment(pedres, 50, false, true));

				paymentRepository.save(createPayment(neurores, 250, false, false));
				paymentRepository.save(createPayment(neurores, 200, true, false));
				paymentRepository.save(createPayment(neurores, 150, false, true));

				System.out.println("Baza de date a fost populată cu servicii și prețuri (Standard, Asigurare, Abonament).");
			}
		};
	}

	private com.unibuc.management.entities.PaymentType createPayment(MedicalService service, int price, boolean insurance, boolean subscription) {
		com.unibuc.management.entities.PaymentType p = new com.unibuc.management.entities.PaymentType();
		p.setMedicalService(service);
		p.setPrice(java.math.BigDecimal.valueOf(price));
		p.setIsDoctor(true);
		p.setWithInsurance(insurance);
		p.setWithSubscription(subscription);
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

	private com.unibuc.management.entities.InsuranceProvider createInsurance(String name, String contact) {
		com.unibuc.management.entities.InsuranceProvider insurance = new com.unibuc.management.entities.InsuranceProvider();
		insurance.setName(name);
		insurance.setContactNumber(contact);
		return insurance;
	}
}
