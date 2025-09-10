package com.costedge.repository;

import com.costedge.model.BomChange;
import com.costedge.model.BomChangeStatus;
import com.costedge.model.BomChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BomChangeRepository extends JpaRepository<BomChange, Long> {

    Optional<BomChange> findByPartNumber(String partNumber);
    boolean existsByPartNumber(String partNumber);

    List<BomChange> findByStatus(BomChangeStatus status);
    List<BomChange> findByDepartment(String department);
    List<BomChange> findByModel(String model);
    List<BomChange> findBySupplier(String supplier);
    List<BomChange> findByChangeType(BomChangeType changeType);

    List<BomChange> findByEffectiveDateBetween(LocalDate startDate, LocalDate endDate);
    List<BomChange> findByEffectiveDateAfter(LocalDate date);
    List<BomChange> findByEffectiveDateBefore(LocalDate date);

    // Custom search across multiple fields
    @Query("SELECT b FROM BomChange b WHERE " +
            "LOWER(b.partName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.partNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.supplier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BomChange> searchBomChanges(@Param("searchTerm") String searchTerm);

    List<BomChange> findByModelAndStatus(String model, BomChangeStatus status);
    List<BomChange> findBySupplierAndChangeType(String supplier, BomChangeType changeType);

    List<BomChange> findAllByOrderByEffectiveDateDesc();
    List<BomChange> findAllByOrderByEffectiveDateAsc();

    @Query("SELECT b FROM BomChange b WHERE b.impact > :impactValue")
    List<BomChange> findByImpactGreaterThan(@Param("impactValue") Double impactValue);

    @Query("SELECT b FROM BomChange b WHERE b.impact < :impactValue")
    List<BomChange> findByImpactLessThan(@Param("impactValue") Double impactValue);

    @Query("SELECT b.model, COUNT(b), SUM(b.impact) FROM BomChange b GROUP BY b.model")
    List<Object[]> getSummaryByModel();

    @Query("SELECT b.changeType, COUNT(b), SUM(b.impact) FROM BomChange b GROUP BY b.changeType")
    List<Object[]> getSummaryByChangeType();
}
