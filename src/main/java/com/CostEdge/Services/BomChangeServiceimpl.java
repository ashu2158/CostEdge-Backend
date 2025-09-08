package com.CostEdge.Services;

import com.CostEdge.Model.BomChange;
import com.CostEdge.Model.BomChangeStatus;
import com.CostEdge.Model.BomChangeType;
import com.CostEdge.Repository.BomChangeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional
public class BomChangeServiceimpl {

    private final BomChangeRepository bomChangeRepository;

    @Autowired
    public BomChangeServiceimpl(BomChangeRepository bomChangeRepository) {
        this.bomChangeRepository = bomChangeRepository;
    }

    // Basic CRUD operations
    public List<BomChange> getAllBomChanges() {
        return bomChangeRepository.findAllByOrderByEffectiveDateDesc();
    }

    public Optional<BomChange> getBomChangeById(Long id) {
        return bomChangeRepository.findById(id);
    }

    public Optional<BomChange> getBomChangeByPartNumber(String partNumber) {
        return bomChangeRepository.findByPartNumber(partNumber);
    }

    public BomChange saveBomChange(BomChange bomChange) {
        // Ensure impact is calculated
        if (bomChange.getImpact() == null && bomChange.getOldCost() != null && bomChange.getNewCost() != null) {
            bomChange.setImpact(bomChange.getNewCost().subtract(bomChange.getOldCost()));
        }
        return bomChangeRepository.save(bomChange);
    }

    public List<BomChange> saveAllBomChanges(List<BomChange> bomChanges) {
        // Calculate impact for each record if not already set
        bomChanges.forEach(bomChange -> {
            if (bomChange.getImpact() == null && bomChange.getOldCost() != null && bomChange.getNewCost() != null) {
                bomChange.setImpact(bomChange.getNewCost().subtract(bomChange.getOldCost()));
            }
        });
        return bomChangeRepository.saveAll(bomChanges);
    }

    public void deleteBomChange(Long id) {
        bomChangeRepository.deleteById(id);
    }

    public boolean existsByPartNumber(String partNumber) {
        return bomChangeRepository.existsByPartNumber(partNumber);
    }

    // Query methods
    public List<BomChange> getBomChangesByStatus(BomChangeStatus status) {
        return bomChangeRepository.findByStatus(status);
    }

    public List<BomChange> getBomChangesByDepartment(String department) {
        return bomChangeRepository.findByDepartment(department);
    }

    public List<BomChange> getBomChangesByModel(String model) {
        return bomChangeRepository.findByModel(model);
    }

    public List<BomChange> getBomChangesBySupplier(String supplier) {
        return bomChangeRepository.findBySupplier(supplier);
    }

    public List<BomChange> getBomChangesByChangeType(BomChangeType changeType) {
        return bomChangeRepository.findByChangeType(changeType);
    }

    public List<BomChange> getBomChangesByEffectiveDateRange(LocalDate startDate, LocalDate endDate) {
        return bomChangeRepository.findByEffectiveDateBetween(startDate, endDate);
    }

    public List<BomChange> searchBomChanges(String searchTerm) {
        return bomChangeRepository.searchBomChanges(searchTerm);
    }

    // Excel processing method
    public List<BomChange> processExcelFile(MultipartFile file) throws IOException {
        List<BomChange> bomChanges = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    BomChange bomChange = createBomChangeFromRow(row);
                    if (bomChange != null) {
                        bomChanges.add(bomChange);
                    }
                } catch (Exception e) {
                    // Log error for this row and continue
                    System.err.println("Error processing row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }

        return bomChanges;
    }

    private BomChange createBomChangeFromRow(Row row) {
        try {
            BomChange bomChange = new BomChange();

            // Expected column order based on your frontend:
            // Model, Part Name, Part Number, Old Cost, New Cost, Supplier, Effective Date,
            // Change Type, Status, Department, Remarks

            bomChange.setModel(getCellValueAsString(row.getCell(0)));
            bomChange.setPartName(getCellValueAsString(row.getCell(1)));
            bomChange.setPartNumber(getCellValueAsString(row.getCell(2)));
            bomChange.setOldCost(getCellValueAsBigDecimal(row.getCell(3)));
            bomChange.setNewCost(getCellValueAsBigDecimal(row.getCell(4)));
            bomChange.setSupplier(getCellValueAsString(row.getCell(5)));
            bomChange.setEffectiveDate(getCellValueAsLocalDate(row.getCell(6)));
            bomChange.setChangeType(getCellValueAsChangeType(row.getCell(7)));
            bomChange.setStatus(getCellValueAsStatus(row.getCell(8)));
            bomChange.setDepartment(getCellValueAsString(row.getCell(9)));
            bomChange.setRemarks(getCellValueAsString(row.getCell(10)));

            // Calculate impact
            if (bomChange.getOldCost() != null && bomChange.getNewCost() != null) {
                bomChange.setImpact(bomChange.getNewCost().subtract(bomChange.getOldCost()));
            }

            return bomChange;
        } catch (Exception e) {
            throw new RuntimeException("Error creating BomChange from row: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().replaceAll("[^\\d.-]", ""));
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            default:
                return BigDecimal.ZERO;
        }
    }

    private LocalDate getCellValueAsLocalDate(Cell cell) {
        if (cell == null) return LocalDate.now();

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }
                break;
            case STRING:
                try {
                    return LocalDate.parse(cell.getStringCellValue());
                } catch (Exception e) {
                    return LocalDate.now();
                }
        }
        return LocalDate.now();
    }

    private BomChangeType getCellValueAsChangeType(Cell cell) {
        if (cell == null) return BomChangeType.NEW_PART;

        String value = getCellValueAsString(cell).toUpperCase();
        try {
            return BomChangeType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Default fallback
            return BomChangeType.NEW_PART;
        }
    }

    private BomChangeStatus getCellValueAsStatus(Cell cell) {
        if (cell == null) return BomChangeStatus.PENDING;

        String value = getCellValueAsString(cell).toUpperCase();
        try {
            return BomChangeStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Default fallback
            return BomChangeStatus.PENDING;
        }
    }

    // Summary methods
    public Map<String, Object> getModelSummary() {
        List<Object[]> results = bomChangeRepository.getSummaryByModel();
        Map<String, Object> summary = new HashMap<>();

        for (Object[] result : results) {
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("changes", result[1]);
            modelData.put("impact", result[2]);
            summary.put((String) result[0], modelData);
        }

        return summary;
    }

    public Map<String, Object> getChangeTypeSummary() {
        List<Object[]> results = bomChangeRepository.getSummaryByChangeType();
        Map<String, Object> summary = new HashMap<>();

        for (Object[] result : results) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("changes", result[1]);
            typeData.put("impact", result[2]);
            summary.put(result[0].toString(), typeData);
        }

        return summary;
    }

    public List<BomChange> getHighImpactChanges(Double threshold) {
        return bomChangeRepository.findByImpactGreaterThan(threshold);
    }

    public List<BomChange> getCostSavings(Double threshold) {
        return bomChangeRepository.findByImpactLessThan(threshold);
    }
}
