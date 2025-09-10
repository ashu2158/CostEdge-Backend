package com.costedge.services;

import com.costedge.model.ImportCost;
import com.costedge.repository.ImportCostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImportCostServiceimpl implements ImportCostService {

    private final ImportCostRepository importCostRepository;

    @Autowired
    public ImportCostServiceimpl(ImportCostRepository importCostRepository) {
        this.importCostRepository = importCostRepository;
    }

    @Override
    public List<ImportCost> getAllImportCosts() {
        return importCostRepository.findAll();
    }

    @Override
    public List<ImportCost> saveAllImportCosts(List<ImportCost> importCosts) {
        return importCostRepository.saveAll(importCosts);
    }

    @Override
    public List<ImportCost> processExcelFile(MultipartFile file) throws Exception {
        // Your logic to read the Excel file (e.g., using Apache POI) and convert it to a List<ImportCost> goes here.
        // This is a placeholder.
        System.out.println("Processing import cost Excel file: " + file.getOriginalFilename());
        return List.of(); // Return an empty list for now
    }

    @Override
    public List<ImportCost> findBySupplier(String supplier) {
        return importCostRepository.findBySupplier(supplier);
    }
}
