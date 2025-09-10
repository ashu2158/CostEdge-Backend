package com.costedge.controller;

import com.costedge.model.ImportCost;
import com.costedge.services.ImportCostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import-costs")
@CrossOrigin(origins = "*") // Enable CORS globally for this controller
public class ImportCostController {

    private final ImportCostService importCostService;

    @Autowired
    public ImportCostController(ImportCostService importCostService) {
        this.importCostService = importCostService;
    }

    // ✅ Get all import cost records
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImportCost>> getAllImportCosts() {
        try {
            List<ImportCost> records = importCostService.getAllImportCosts();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Save multiple import costs from JSON body
    @PostMapping("/saveAllJson")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DATAENTRY')")
    public ResponseEntity<List<ImportCost>> saveAllImportCostsFromJson(
            @Valid @RequestBody List<ImportCost> importCosts) {
        try {
            List<ImportCost> savedRecords = importCostService.saveAllImportCosts(importCosts);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Save multiple import costs from Excel upload
    @PostMapping("/saveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DATAENTRY')")
    public ResponseEntity<List<ImportCost>> saveAllFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<ImportCost> importCosts = importCostService.processExcelFile(file);
            List<ImportCost> savedRecords = importCostService.saveAllImportCosts(importCosts);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
