package com.CostEdge.Repository;

import com.CostEdge.Model.BomChange;
import com.CostEdge.Model.BomChangeStatus;
import com.CostEdge.Model.BomChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BomChangeRepository extends JpaRepository<BomChange, Long> {

    // Find by part number
    Optional<BomChange> findByPartNumber(String partNumber);

    // Check if part number exists
    boolean existsByPartNumber(String partNumber);

    // Find by status
    List<BomChange> findByStatus(BomChangeStatus status);

    // Find by department
    List<BomChange> findByDepartment(String department);

    // Find by model
    List<BomChange> findByModel(String model);

    // Find by supplier
    List<BomChange> findBySupplier(String supplier);

    // Find by change type
    List<BomChange> findByChangeType(BomChangeType changeType);

    // Find by effective date range
    List<BomChange> findByEffectiveDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by effective date after
    List<BomChange> findByEffectiveDateAfter(LocalDate date);

    // Find by effective date before
    List<BomChange> findByEffectiveDateBefore(LocalDate date);

    // Custom query to search across multiple fields
    @Query("SELECT b FROM BomChange b WHERE " +
            "LOWER(b.partName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.partNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.supplier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BomChange> searchBomChanges(@Param("searchTerm") String searchTerm);

    // Find by model and status
    List<BomChange> findByModelAndStatus(String model, BomChangeStatus status);

    // Find by supplier and change type
    List<BomChange> findBySupplierAndChangeType(String supplier, BomChangeType changeType);

    // Get all ordered by effective date descending
    List<BomChange> findAllByOrderByEffectiveDateDesc();

    // Get all ordered by effective date ascending
    List<BomChange> findAllByOrderByEffectiveDateAsc();

    // Find records with impact greater than specified value
    @Query("SELECT b FROM BomChange b WHERE b.impact > :impactValue")
    List<BomChange> findByImpactGreaterThan(@Param("impactValue") Double impactValue);

    // Find records with impact less than specified value
    @Query("SELECT b FROM BomChange b WHERE b.impact < :impactValue")
    List<BomChange> findByImpactLessThan(@Param("impactValue") Double impactValue);

    // Get summary statistics by model
    @Query("SELECT b.model, COUNT(b), SUM(b.impact) FROM BomChange b GROUP BY b.model")
    List<Object[]> getSummaryByModel();

    // Get summary statistics by change type
    @Query("SELECT b.changeType, COUNT(b), SUM(b.impact) FROM BomChange b GROUP BY b.changeType")
    List<Object[]> getSummaryByChangeType();
}