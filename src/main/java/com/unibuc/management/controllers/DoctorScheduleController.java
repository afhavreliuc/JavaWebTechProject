package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.services.DoctorScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    @GetMapping("/day")
    public ResponseEntity<List<ScheduleEntry>> getDoctorScheduleForDay(
            @RequestParam Integer doctorId,
            @RequestParam String date) {

        LocalDate localDate = LocalDate.parse(date);
        List<ScheduleEntry> schedule = doctorScheduleService.getDoctorScheduleForDay(doctorId, localDate);

        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/schedulePTO")
    public ResponseEntity<String> schedulePTO(@RequestParam Integer doctorId,
                                              @RequestParam String startDate,
                                              @RequestParam String endDate) {
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