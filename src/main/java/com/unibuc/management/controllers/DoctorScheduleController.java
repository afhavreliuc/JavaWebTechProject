package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.services.DoctorScheduleService;
import com.unibuc.management.services.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;
    private final DoctorService doctorService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService, DoctorService doctorService) {
        this.doctorScheduleService = doctorScheduleService;
        this.doctorService = doctorService;
    }

    @GetMapping("/day")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<ScheduleEntry>> getDoctorScheduleForDay(
            @RequestParam Integer doctorId,
            @RequestParam String date) {

        LocalDate localDate = LocalDate.parse(date);
        List<ScheduleEntry> schedule = doctorScheduleService.getDoctorScheduleForDay(doctorId, localDate);

        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/schedulePTO")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> schedulePTO(@RequestParam Integer doctorId,
                                              @RequestParam String startDate,
                                              @RequestParam String endDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Doctor> currentDoctorOpt = doctorService.getDoctorByUsername(currentUsername);

        if (currentDoctorOpt.isEmpty() || !currentDoctorOpt.get().getId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only schedule PTO for yourself.");
        }

        OffsetDateTime start = OffsetDateTime.parse(startDate);
        OffsetDateTime end = OffsetDateTime.parse(endDate);
        String result = doctorScheduleService.schedulePTO(doctorId, start, end);

        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
}