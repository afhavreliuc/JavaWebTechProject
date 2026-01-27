package com.unibuc.management.controllers;

import com.unibuc.management.services.PropagationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/propagation")
public class PropagationController {

    @Autowired
    private PropagationService propagationService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncData() {
        try {
            propagationService.propagateData();
            return ResponseEntity.ok("Sincronizare OLTP -> DW realizată cu succes!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Eroare la sincronizare: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(propagationService.getSyncStats());
    }

    @GetMapping("/report/financial-evolution")
    public ResponseEntity<java.util.List<Map<String, Object>>> getFinancialEvolution() {
        return ResponseEntity.ok(propagationService.getFinancialEvolution());
    }

    @GetMapping("/report/top-doctors")
    public ResponseEntity<java.util.List<Map<String, Object>>> getTopDoctors() {
        return ResponseEntity.ok(propagationService.getTopDoctors());
    }
}

