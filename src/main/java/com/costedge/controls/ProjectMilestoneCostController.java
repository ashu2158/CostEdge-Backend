package com.costedge.controller;

import com.costedge.model.ProjectMilestoneCost;
import com.costedge.service.ProjectMilestoneCostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/milestones")
@CrossOrigin(origins = "*")
public class ProjectMilestoneCostController {

    private final ProjectMilestoneCostService service;

    // ✅ Constructor-based injection (no Lombok)
    public ProjectMilestoneCostController(ProjectMilestoneCostService service) {
        this.service = service;
    }

    // A placeholder for testing the MANAGER role.
    @GetMapping("/test-secured")
    @PreAuthorize("hasRole('MANAGER')")
    public String getMilestones() {
        return "This is a secured endpoint for MANAGERS.";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DATAENTRY')")
    public ResponseEntity<?> create(@RequestBody ProjectMilestoneCost cost) {
        try {
            if (cost.getProjectID() == null || cost.getProjectID() <= 0) {
                return ResponseEntity.badRequest().body("Project ID is required and must be positive");
            }
            if (cost.getProjectName() == null || cost.getProjectName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Project name is required");
            }
            if (cost.getMilestone() == null || cost.getMilestone().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Milestone name is required");
            }

            ProjectMilestoneCost savedCost = service.save(cost);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCost);
        } catch (Exception e) {
            System.err.println("Error saving milestone cost: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectMilestoneCost>> getAll() {
        try {
            List<ProjectMilestoneCost> costs = service.getAll();
            return ResponseEntity.ok(costs);
        } catch (Exception e) {
            System.err.println("Error retrieving milestone costs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectMilestoneCost> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectID}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectMilestoneCost>> getByProjectID(@PathVariable Integer projectID) {
        try {
            List<ProjectMilestoneCost> costs = service.getByProjectID(projectID);
            return ResponseEntity.ok(costs);
        } catch (Exception e) {
            System.err.println("Error retrieving costs for project ID " + projectID + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/name/{projectName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectMilestoneCost>> getByProjectName(@PathVariable String projectName) {
        try {
            List<ProjectMilestoneCost> costs = service.getByProjectName(projectName);
            return ResponseEntity.ok(costs);
        } catch (Exception e) {
            System.err.println("Error retrieving costs for project name " + projectName + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateApprovalStatus(@PathVariable Long id, @RequestBody Map<String, Object> approvalData) {
        try {
            Optional<ProjectMilestoneCost> optionalMilestone = service.getById(id);
            if (optionalMilestone.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ProjectMilestoneCost milestone = optionalMilestone.get();
            String approvalStatus = (String) approvalData.get("approvalStatus");

            if (approvalStatus == null || (!approvalStatus.equals("Approved") && !approvalStatus.equals("Rejected"))) {
                return ResponseEntity.badRequest().body("Invalid approval status. Must be 'Approved' or 'Rejected'");
            }

            milestone.setApprovalStatus(approvalStatus);
            milestone.setApprovedBy((String) approvalData.get("approvedBy"));
            milestone.setApprovedAt(LocalDateTime.now());
            milestone.setRemarks((String) approvalData.get("remarks"));

            if ("Rejected".equals(approvalStatus)) {
                milestone.setRejectionReason((String) approvalData.get("rejectionReason"));
            } else {
                milestone.setRejectionReason(null);
            }

            ProjectMilestoneCost updatedMilestone = service.save(milestone);
            return ResponseEntity.ok(updatedMilestone);
        } catch (Exception e) {
            System.err.println("Error updating approval status for milestone " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/approval-status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectMilestoneCost>> getByApprovalStatus(@PathVariable String status) {
        try {
            List<ProjectMilestoneCost> costs = service.getByApprovalStatus(status);
            return ResponseEntity.ok(costs);
        } catch (Exception e) {
            System.err.println("Error retrieving costs by approval status " + status + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATAENTRY')")
    public ResponseEntity<ProjectMilestoneCost> update(@PathVariable Long id, @RequestBody ProjectMilestoneCost cost) {
        try {
            if (service.getById(id).isPresent()) {
                cost.setId(id);
                ProjectMilestoneCost updatedCost = service.save(cost);
                return ResponseEntity.ok(updatedCost);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating milestone cost: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            if (service.getById(id).isPresent()) {
                service.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error deleting milestone cost: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
