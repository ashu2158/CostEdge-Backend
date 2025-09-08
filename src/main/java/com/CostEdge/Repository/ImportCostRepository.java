package com.CostEdge.Repository;

import com.CostEdge.Model.ImportCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportCostRepository extends JpaRepository<ImportCost, Long> {

    // Spring Data JPA will automatically create a query to find records by supplier
    List<ImportCost> findBySupplier(String supplier);

}
