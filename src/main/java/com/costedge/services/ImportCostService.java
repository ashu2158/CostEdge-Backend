package com.costedge.services;

import com.costedge.model.ImportCost;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportCostService {

    List<ImportCost> getAllImportCosts();

    List<ImportCost> saveAllImportCosts(List<ImportCost> importCosts);

    List<ImportCost> processExcelFile(MultipartFile file) throws Exception;

    List<ImportCost> findBySupplier(String supplier);
}
