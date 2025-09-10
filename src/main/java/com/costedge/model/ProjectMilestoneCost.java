package com.costedge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_milestone_costs")
public class ProjectMilestoneCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Integer projectID;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @NotBlank(message = "Milestone name is required")
    @Size(max = 255, message = "Milestone name must not exceed 255 characters")
    @Column(nullable = false)
    private String milestone;

    @NotNull(message = "Planned cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Planned cost must be positive")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal planned;

    @NotNull(message = "Actual cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Actual cost must be positive")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal actual;

    @Column(precision = 15, scale = 2)
    private BigDecimal variance;

    @NotNull(message = "Project quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Project quantity must be positive")
    @Column(name = "project_quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal projectQuantity;

    @NotBlank(message = "Reason is required")
    @Column(nullable = false, length = 1000)
    private String reason;

    @NotNull(message = "Milestone date is required")
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "Milestone type is required")
    @Column(name = "milestone_type", nullable = false)
    private String milestoneType;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "part_number")
    private String partNumber;

    @NotNull(message = "Expected completion date is required")
    @Column(name = "expected_completion_date", nullable = false)
    private LocalDate expectedCompletionDate;


    @Column(name = "approved_by")
    private String approvedBy;

    @Column(length = 1000)
    private String remarks;

    // NEW FIELD: Rejection reason for RFA
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // NEW FIELD: Approval timestamp
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "documents_links", length = 2000)
    private String documentsLinks;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @Column(name = "status", nullable = false)
    private String approvalStatus = "Pending";

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "current_percent", precision = 5, scale = 2)
    private BigDecimal currentPercent;

    @Column(name = "target_percent", precision = 5, scale = 2)
    private BigDecimal targetPercent;

    @Column(name = "estimated_savings", precision = 15, scale = 2)
    private BigDecimal estimatedSavings;

    @Column(name = "cost_reduction_status")
    private String costReductionStatus;

    // Default constructor (required by JPA)
    public ProjectMilestoneCost() {
    }

    // Constructor with project ID
    public ProjectMilestoneCost(Integer projectID) {
        this.projectID = projectID;
    }

    // Constructor with essential fields
    public ProjectMilestoneCost(String projectName, String milestone, String milestoneType,
                                String department, BigDecimal planned, BigDecimal actual,
                                LocalDate date, String reason, String category, Integer projectID,
                                BigDecimal projectQuantity) {
        this.projectName = projectName;
        this.milestone = milestone;
        this.milestoneType = milestoneType;
        this.department = department;
        this.planned = planned;
        this.actual = actual;
        this.date = date;
        this.reason = reason;
        this.category = category;
        this.projectID = projectID;
        this.projectQuantity = projectQuantity;
        this.variance = calculateVariance();
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (variance == null) {
            variance = calculateVariance();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        variance = calculateVariance();
    }

    // Business logic methods
    public BigDecimal calculateVariance() {
        if (planned != null && actual != null) {
            return actual.subtract(planned);
        }
        return BigDecimal.ZERO;
    }

    public boolean isOverBudget() {
        return variance != null && variance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isUnderBudget() {
        return variance != null && variance.compareTo(BigDecimal.ZERO) < 0;
    }

    // Additional business methods for quantity-based calculations
    public BigDecimal getUnitPlannedCost() {
        if (planned != null && projectQuantity != null && projectQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return planned.divide(projectQuantity, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getUnitActualCost() {
        if (actual != null && projectQuantity != null && projectQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return actual.divide(projectQuantity, 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public BigDecimal getPlanned() {
        return planned;
    }

    public void setPlanned(BigDecimal planned) {
        this.planned = planned;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public void setActual(BigDecimal actual) {
        this.actual = actual;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public BigDecimal getProjectQuantity() {
        return projectQuantity;
    }

    public void setProjectQuantity(BigDecimal projectQuantity) {
        this.projectQuantity = projectQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(String milestoneType) {
        this.milestoneType = milestoneType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }



    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // NEW GETTER/SETTER for rejection reason
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    // NEW GETTER/SETTER for approval timestamp
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getDocumentsLinks() {
        return documentsLinks;
    }

    public void setDocumentsLinks(String documentsLinks) {
        this.documentsLinks = documentsLinks;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(BigDecimal currentPercent) {
        this.currentPercent = currentPercent;
    }

    public BigDecimal getTargetPercent() {
        return targetPercent;
    }

    public void setTargetPercent(BigDecimal targetPercent) {
        this.targetPercent = targetPercent;
    }

    public BigDecimal getEstimatedSavings() {
        return estimatedSavings;
    }

    public void setEstimatedSavings(BigDecimal estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }

    public String getCostReductionStatus() {
        return costReductionStatus;
    }

    public void setCostReductionStatus(String costReductionStatus) {
        this.costReductionStatus = costReductionStatus;
    }

    @Override
    public String toString() {
        return "ProjectMilestoneCost{" +
                "id=" + id +
                ", projectID=" + projectID +
                ", projectName='" + projectName + '\'' +
                ", milestone='" + milestone + '\'' +
                ", milestoneType='" + milestoneType + '\'' +
                ", department='" + department + '\'' +
                ", planned=" + planned +
                ", actual=" + actual +
                ", variance=" + variance +
                ", projectQuantity=" + projectQuantity +
                ", currency='" + currency + '\'' +
//                ", status='" + status + '\'' +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", date=" + date +
                ", expectedCompletionDate=" + expectedCompletionDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMilestoneCost that = (ProjectMilestoneCost) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}