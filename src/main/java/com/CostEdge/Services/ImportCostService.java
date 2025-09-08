package com.CostEdge.Services;

import com.CostEdge.Model.ImportCost;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportCostService {

    List<ImportCost> getAllImportCosts();

    List<ImportCost> saveAllImportCosts(List<ImportCost> importCosts);

    List<ImportCost> processExcelFile(MultipartFile file) throws Exception;

    List<ImportCost> findBySupplier(String supplier);
}
