package com.unibuc.management.controllers;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class MedicalServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalServiceController medicalServiceController;

    private List<MedicalService> medicalServices;

    @BeforeEach
    public void setup() {
        MedicalService medicalService = new MedicalService();
        medicalService.setName("General Medicine");
        medicalService.setSpecialization("General");
        medicalService.setStartHour(9);
        medicalService.setEndHour(17);
        medicalService.setRating(4.5);
        medicalService.setNrOfRatings(10);

        MedicalService medicalService2 = new MedicalService();
        medicalService.setName("Cardiology");
        medicalService.setSpecialization("Cardiology");
        medicalService.setStartHour(9);
        medicalService.setEndHour(17);
        medicalService.setRating(4.8);
        medicalService.setNrOfRatings(12);

        // Create some mock data for MedicalService
        medicalServices = Arrays.asList(
                medicalService,
                medicalService2
        );
    }

    @Test
    public void testGetAllMedicalServices() throws Exception {
        // Mock the repository to return predefined services
        when(medicalServiceRepository.findAll()).thenReturn(medicalServices);

        mockMvc.perform(get("/medical-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("General Medicine"))
                .andExpect(jsonPath("$[1].name").value("Cardiology"));
    }

    @Test
    public void testGetMedicalServicesBySpecialization() throws Exception {
        String specialization = "Cardiology";

        // Mock the repository to return services for the given specialization
        when(medicalServiceRepository.findBySpecialization(specialization)).thenReturn(
                Arrays.asList(medicalServices.get(1)) // Return only the Cardiology service
        );

        mockMvc.perform(get("/medical-services/specialization/{specialization}", specialization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cardiology"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));
    }
}

