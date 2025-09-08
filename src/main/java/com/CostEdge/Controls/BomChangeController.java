package com.CostEdge.Controls;

import com.CostEdge.Model.BomChange;
import com.CostEdge.Model.BomChangeStatus;
import com.CostEdge.Model.BomChangeType;
import com.CostEdge.Services.BomChangeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bom-changes")
@CrossOrigin(origins = "*")
public class BomChangeController {

    private final BomChangeService bomChangeService;

    @Autowired
    public BomChangeController(BomChangeService bomChangeService) {
        this.bomChangeService = bomChangeService;
    }

    // Test endpoint for role verification
    @GetMapping("/test-secured")
    @PreAuthorize("hasRole('MANAGER')")
    public String testSecured() {
        return "This is a secured BOM changes endpoint for MANAGERS.";
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()") // Any logged-in user can view
    public ResponseEntity<List<BomChange>> getAllBomChanges() {
        try {
            List<BomChange> bomChanges = bomChangeService.getAllBomChanges();
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BomChange> getBomChangeById(@PathVariable Long id) {
        try {
            Optional<BomChange> bomChange = bomChangeService.getBomChangeById(id);
            return bomChange.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/part-number/{partNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BomChange> getBomChangeByPartNumber(@PathVariable String partNumber) {
        try {
            Optional<BomChange> bomChange = bomChangeService.getBomChangeByPartNumber(partNumber);
            return bomChange.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')") // Only ADMIN/MANAGER can create
    public ResponseEntity<?> addBomChange(@Valid @RequestBody BomChange bomChange, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
            }

            BomChange savedBomChange = bomChangeService.saveBomChange(bomChange);
            return new ResponseEntity<>(savedBomChange, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save BOM change: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/saveAllJson")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATAENTRY')") // Only ADMIN/MANAGER can bulk create
    public ResponseEntity<?> saveAllBomChangesFromJson(@Valid @RequestBody List<BomChange> bomChanges, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Validation failed");
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                errorResponse.put("errors", errors);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            if (bomChanges == null || bomChanges.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No BOM changes provided");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> savedBomChanges = bomChangeService.saveAllBomChanges(bomChanges);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully saved " + savedBomChanges.size() + " BOM changes");
            response.put("data", savedBomChanges);

            return new ResponseEntity<>(savedBomChanges, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save BOM changes: " + e.getMessage());
            error.put("details", e.getCause() != null ? e.getCause().getMessage() : "No additional details");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/saveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATAENTRY')") // Only ADMIN/MANAGER can upload files
    public ResponseEntity<?> saveAllBomChanges(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please select a file to upload");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Only Excel files (.xlsx, .xls) are supported");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        try {
            List<BomChange> bomChanges = bomChangeService.processExcelFile(file);

            if (bomChanges.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No valid data found in the Excel file");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> savedBomChanges = bomChangeService.saveAllBomChanges(bomChanges);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully imported " + savedBomChanges.size() + " records from Excel file");
            response.put("data", savedBomChanges);

            return new ResponseEntity<>(savedBomChanges, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process Excel file: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')") // Only ADMIN/MANAGER can update
    public ResponseEntity<?> updateBomChange(@PathVariable Long id,
                                             @Valid @RequestBody BomChange bomChange,
                                             BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
            }

            Optional<BomChange> existingBomChange = bomChangeService.getBomChangeById(id);
            if (existingBomChange.isPresent()) {
                bomChange.setId(id);
                BomChange updatedBomChange = bomChangeService.saveBomChange(bomChange);
                return new ResponseEntity<>(updatedBomChange, HttpStatus.OK);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "BOM change not found with ID: " + id);
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update BOM change: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can delete
    public ResponseEntity<?> deleteBomChange(@PathVariable Long id) {
        try {
            Optional<BomChange> existingBomChange = bomChangeService.getBomChangeById(id);
            if (existingBomChange.isPresent()) {
                bomChangeService.deleteBomChange(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "BOM change deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "BOM change not found with ID: " + id);
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete BOM change: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesByStatus(@PathVariable String status) {
        try {
            BomChangeStatus bomStatus;
            try {
                bomStatus = BomChangeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status + ". Valid statuses are: PENDING, APPROVED, REJECTED, COMPLETED");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesByStatus(bomStatus);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by status: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesByDepartment(@PathVariable String department) {
        try {
            if (department == null || department.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Department name cannot be empty");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesByDepartment(department);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by department: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/model/{model}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesByModel(@PathVariable String model) {
        try {
            if (model == null || model.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Model name cannot be empty");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesByModel(model);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by model: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/supplier/{supplier}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesBySupplier(@PathVariable String supplier) {
        try {
            if (supplier == null || supplier.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Supplier name cannot be empty");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesBySupplier(supplier);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by supplier: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/change-type/{changeType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesByChangeType(@PathVariable String changeType) {
        try {
            BomChangeType bomChangeType;
            try {
                bomChangeType = BomChangeType.valueOf(changeType.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid change type: " + changeType + ". Valid types are: NEW_PART, REDUCTION, ADDITION");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesByChangeType(bomChangeType);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by change type: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBomChangesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Both startDate and endDate are required. Format: YYYY-MM-DD");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            if (startDate.isAfter(endDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Start date cannot be after end date");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getBomChangesByEffectiveDateRange(startDate, endDate);
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch BOM changes by date range: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchBomChanges(@RequestParam String term) {
        try {
            if (term == null || term.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Search term cannot be empty");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            if (term.length() < 2) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Search term must be at least 2 characters long");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.searchBomChanges(term.trim());
            return new ResponseEntity<>(bomChanges, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search BOM changes: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary/model")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getModelSummary() {
        try {
            Map<String, Object> summary = bomChangeService.getModelSummary();
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate model summary: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary/change-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getChangeTypeSummary() {
        try {
            Map<String, Object> summary = bomChangeService.getChangeTypeSummary();
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate change type summary: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/high-impact")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getHighImpactChanges(@RequestParam Double threshold) {
        try {
            if (threshold == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Threshold parameter is required");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getHighImpactChanges(threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("threshold", threshold);
            response.put("count", bomChanges.size());
            response.put("data", bomChanges);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch high impact changes: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cost-savings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCostSavings(@RequestParam Double threshold) {
        try {
            if (threshold == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Threshold parameter is required");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<BomChange> bomChanges = bomChangeService.getCostSavings(threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("threshold", threshold);
            response.put("count", bomChanges.size());
            response.put("data", bomChanges);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch cost savings: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getStatistics() {
        try {
            List<BomChange> allChanges = bomChangeService.getAllBomChanges();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalChanges", allChanges.size());
            stats.put("totalImpact", allChanges.stream().mapToDouble(bc -> bc.getImpact().doubleValue()).sum());
            stats.put("averageImpact", allChanges.stream().mapToDouble(bc -> bc.getImpact().doubleValue()).average().orElse(0.0));

            Map<String, Long> statusBreakdown = allChanges.stream()
                    .collect(Collectors.groupingBy(bc -> bc.getStatus().toString(), Collectors.counting()));
            stats.put("statusBreakdown", statusBreakdown);

            Map<String, Long> typeBreakdown = allChanges.stream()
                    .collect(Collectors.groupingBy(bc -> bc.getChangeType().toString(), Collectors.counting()));
            stats.put("changeTypeBreakdown", typeBreakdown);

            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate statistics: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDate.now().toString());
        health.put("service", "BOM Changes API");
        return new ResponseEntity<>(health, HttpStatus.OK);
    }
}