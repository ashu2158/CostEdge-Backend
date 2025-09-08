package com.CostEdge.Controls;

import com.CostEdge.Model.ImportCost;
import com.CostEdge.Services.ImportCostService;
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
@CrossOrigin(origins = "*") // Add this for CORS support
public class ImportCostController {

    private final ImportCostService importCostService;

    @Autowired
    public ImportCostController(ImportCostService importCostService) {
        this.importCostService = importCostService;
    }

    // API to get all import cost records
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ImportCost>> getAllImportCosts() {
        try {
            List<ImportCost> records = importCostService.getAllImportCosts();
            return new ResponseEntity<>(records, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API for the multi-add form (submitting JSON)
    @PostMapping("/saveAllJson")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DATAENTRY')")
    public ResponseEntity<List<ImportCost>> saveAllImportCostsFromJson(@Valid @RequestBody List<ImportCost> importCosts) {
        try {
            List<ImportCost> savedRecords = importCostService.saveAllImportCosts(importCosts);
            return new ResponseEntity<>(savedRecords, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API for the Excel file import
    @PostMapping("/saveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DATAENTRY')")
    public ResponseEntity<List<ImportCost>> saveAllFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            List<ImportCost> importCosts = importCostService.processExcelFile(file);
            List<ImportCost> savedRecords = importCostService.saveAllImportCosts(importCosts);
            return new ResponseEntity<>(savedRecords, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
