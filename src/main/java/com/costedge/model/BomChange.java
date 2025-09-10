package com.costedge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bom_changebox")
public class BomChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model cannot exceed 100 characters")
    @Column(name = "model", nullable = false)
    private String model;

    @NotBlank(message = "Part name is required")
    @Size(max = 255, message = "Part name cannot exceed 255 characters")
    @Column(name = "part_name", nullable = false)
    private String partName;

    @NotBlank(message = "Part number is required")
    @Size(max = 100, message = "Part number cannot exceed 100 characters")
    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @NotNull(message = "Old cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Old cost must be greater than or equal to 0")
    @Column(name = "old_cost", precision = 10, scale = 2)
    private BigDecimal oldCost;

    @NotNull(message = "New cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "New cost must be greater than or equal to 0")
    @Column(name = "new_cost", precision = 10, scale = 2)
    private BigDecimal newCost;

    @Column(name = "impact", precision = 10, scale = 2)
    private BigDecimal impact;

    @NotBlank(message = "Supplier is required")
    @Size(max = 255, message = "Supplier name cannot exceed 255 characters")
    @Column(name = "supplier", nullable = false)
    private String supplier;

    @NotNull(message = "Effective date is required")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @NotNull(message = "Change type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private BomChangeType changeType;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BomChangeStatus status;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    @Column(name = "department", nullable = false)
    private String department;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Column(name = "remarks")
    private String remarks;

    @Column(name = "document")
    private String document;

    // ✅ New Quantity field
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Constructors
    public BomChange() {
    }

    public BomChange(String model, String partName, String partNumber, BigDecimal oldCost,
                     BigDecimal newCost, BigDecimal impact, String supplier,
                     LocalDate effectiveDate, BomChangeType changeType,
                     BomChangeStatus status, String department, String remarks, String document,
                     Integer quantity) {
        this.model = model;
        this.partName = partName;
        this.partNumber = partNumber;
        this.oldCost = oldCost;
        this.newCost = newCost;
        this.impact = impact;
        this.supplier = supplier;
        this.effectiveDate = effectiveDate;
        this.changeType = changeType;
        this.status = status;
        this.department = department;
        this.remarks = remarks;
        this.document = document;
        this.quantity = quantity;
    }

    // Calculate impact automatically before saving
    @PrePersist
    @PreUpdate
    private void calculateImpact() {
        if (this.oldCost != null && this.newCost != null) {
            this.impact = this.newCost.subtract(this.oldCost);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public BigDecimal getOldCost() { return oldCost; }
    public void setOldCost(BigDecimal oldCost) { this.oldCost = oldCost; }

    public BigDecimal getNewCost() { return newCost; }
    public void setNewCost(BigDecimal newCost) { this.newCost = newCost; }

    public BigDecimal getImpact() { return impact; }
    public void setImpact(BigDecimal impact) { this.impact = impact; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BomChangeType getChangeType() { return changeType; }
    public void setChangeType(BomChangeType changeType) { this.changeType = changeType; }

    public BomChangeStatus getStatus() { return status; }
    public void setStatus(BomChangeStatus status) { this.status = status; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    // Approval-related setters (for future use)
    public void setApprovalStatus(String approvalStatus) {}
    public void setApprovedBy(String s) {}
    public void setApprovedAt(LocalDateTime now) {}
    public void setManagerRemarks(String remarks) {}
    public void setRejectionReason(String s) {}
}
