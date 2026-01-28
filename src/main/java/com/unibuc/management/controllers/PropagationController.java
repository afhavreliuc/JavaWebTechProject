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
            System.err.println("[Controller] Error during sync: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Cauză: " + e.getCause().getMessage();
            }
            return ResponseEntity.internalServerError().body("Eroare la sincronizare: " + errorMessage);
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

    @GetMapping("/report/pareto-analysis")
    public ResponseEntity<java.util.List<Map<String, Object>>> getParetoAnalysis() {
        return ResponseEntity.ok(propagationService.getParetoAnalysis());
    }
}

