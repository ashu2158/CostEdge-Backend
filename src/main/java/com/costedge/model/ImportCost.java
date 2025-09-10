package com.costedge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_costs")
public class ImportCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", unique = true, nullable = false)
    private String shipmentId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "supplier", nullable = false)
    private String supplier;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "freight", precision = 10, scale = 2)
    private BigDecimal freight = BigDecimal.ZERO;

    @Column(name = "duty", precision = 10, scale = 2)
    private BigDecimal duty = BigDecimal.ZERO;

    @Column(name = "insurance", precision = 10, scale = 2)
    private BigDecimal insurance = BigDecimal.ZERO;

    @Column(name = "document")
    private String document;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Calculate total cost dynamically (multiplied by quantity)
    public BigDecimal getTotalCost() {
        return (freight.add(duty).add(insurance)).multiply(BigDecimal.valueOf(quantity));
    }

    // Constructors
    public ImportCost() {}

    public ImportCost(String shipmentId, LocalDate date, String supplier,
                      String model, String partName, Integer quantity,
                      BigDecimal freight, BigDecimal duty, BigDecimal insurance) {
        this.shipmentId = shipmentId;
        this.date = date;
        this.supplier = supplier;
        this.model = model;
        this.partName = partName;
        this.quantity = quantity != null ? quantity : 1;
        this.freight = freight != null ? freight : BigDecimal.ZERO;
        this.duty = duty != null ? duty : BigDecimal.ZERO;
        this.insurance = insurance != null ? insurance : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity != null ? quantity : 1; }

    public BigDecimal getFreight() { return freight; }
    public void setFreight(BigDecimal freight) { this.freight = freight != null ? freight : BigDecimal.ZERO; }

    public BigDecimal getDuty() { return duty; }
    public void setDuty(BigDecimal duty) { this.duty = duty != null ? duty : BigDecimal.ZERO; }

    public BigDecimal getInsurance() { return insurance; }
    public void setInsurance(BigDecimal insurance) { this.insurance = insurance != null ? insurance : BigDecimal.ZERO; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
