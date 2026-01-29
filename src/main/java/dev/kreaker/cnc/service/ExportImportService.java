package dev.kreaker.cnc.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.CatalogItemDTO;
import dev.kreaker.cnc.service.dto.ConversionDTO;
import dev.kreaker.cnc.service.dto.ImportResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportImportService {

	private final CatalogService catalogService;
	private final ConversionService conversionService;

	// CSV Headers for catalog export
	private static final String[] CATALOG_HEADERS = {
			"Modulo", "Campo", "Valor", "Cadena", "Descripcion", "Source",
			"Has_Conversion", "Conversion_Domain", "Conversion_Status"
	};

	// CSV Headers for conversion import
	private static final String[] CONVERSION_HEADERS = {
			"Modulo", "Campo", "Valor", "Cadena", "Domain", "Status"
	};

	// CSV Headers for full catalog import (with optional conversion)
	private static final String[] CATALOG_IMPORT_HEADERS = {
			"Modulo", "Campo", "Valor", "Cadena", "Descripcion", "Orden", "Domain", "Status"
	};

	/**
	 * Export catalog items to CSV format
	 */
	public byte[] exportToCsv(CatalogFilterDTO filter) throws IOException {
		List<CatalogItemDTO> items = catalogService.getUnifiedCatalog(filter);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
			// Write header
			writer.writeNext(CATALOG_HEADERS);

			// Write data
			for (CatalogItemDTO item : items) {
				String[] row = {
						item.getModulo(),
						item.getCampo(),
						item.getValor(),
						String.valueOf(item.getSbsNo()),
						item.getDescripcion(),
						item.getSourceDisplay(),
						item.isHasConversion() ? "Yes" : "No",
						item.getConversionDomain() != null ? item.getConversionDomain() : "",
						item.getConversionStatus() != null ? String.valueOf(item.getConversionStatus()) : ""
				};
				writer.writeNext(row);
			}
		}

		log.info("Exported {} catalog items to CSV", items.size());
		return outputStream.toByteArray();
	}

	/**
	 * Export catalog items to Excel format
	 */
	public byte[] exportToExcel(CatalogFilterDTO filter) throws IOException {
		List<CatalogItemDTO> items = catalogService.getUnifiedCatalog(filter);

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Catalog");

			// Create header style
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < CATALOG_HEADERS.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(CATALOG_HEADERS[i]);
				cell.setCellStyle(headerStyle);
			}

			// Create data style
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);

			// Write data rows
			int rowNum = 1;
			for (CatalogItemDTO item : items) {
				Row row = sheet.createRow(rowNum++);

				createCell(row, 0, item.getModulo(), dataStyle);
				createCell(row, 1, item.getCampo(), dataStyle);
				createCell(row, 2, item.getValor(), dataStyle);
				createCell(row, 3, item.getSbsNo() != null ? String.valueOf(item.getSbsNo()) : "", dataStyle);
				createCell(row, 4, item.getDescripcion(), dataStyle);
				createCell(row, 5, item.getSourceDisplay(), dataStyle);
				createCell(row, 6, item.isHasConversion() ? "Yes" : "No", dataStyle);
				createCell(row, 7, item.getConversionDomain() != null ? item.getConversionDomain() : "", dataStyle);
				createCell(row, 8, item.getConversionStatus() != null ? String.valueOf(item.getConversionStatus()) : "", dataStyle);
			}

			// Auto-size columns
			for (int i = 0; i < CATALOG_HEADERS.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// Write to byte array
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);

			log.info("Exported {} catalog items to Excel", items.size());
			return outputStream.toByteArray();
		}
	}

	/**
	 * Export only conversions to CSV (for import template)
	 */
	public byte[] exportConversionsToCsv(CatalogFilterDTO filter) throws IOException {
		List<CatalogItemDTO> items = catalogService.getUnifiedCatalog(filter);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
			// Write header
			writer.writeNext(CONVERSION_HEADERS);

			// Write data - only items with conversions or all items for template
			for (CatalogItemDTO item : items) {
				String[] row = {
						item.getModulo(),
						item.getCampo(),
						item.getValor(),
						String.valueOf(item.getSbsNo()),
						item.getConversionDomain() != null ? item.getConversionDomain() : "",
						item.getConversionStatus() != null ? String.valueOf(item.getConversionStatus()) : "1"
				};
				writer.writeNext(row);
			}
		}

		log.info("Exported {} items to conversions CSV template", items.size());
		return outputStream.toByteArray();
	}

	/**
	 * Export only conversions to Excel (for import template)
	 */
	public byte[] exportConversionsToExcel(CatalogFilterDTO filter) throws IOException {
		List<CatalogItemDTO> items = catalogService.getUnifiedCatalog(filter);

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Conversions");

			// Create header style
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < CONVERSION_HEADERS.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(CONVERSION_HEADERS[i]);
				cell.setCellStyle(headerStyle);
			}

			// Create data style
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setBorderBottom(BorderStyle.THIN);
			dataStyle.setBorderTop(BorderStyle.THIN);
			dataStyle.setBorderLeft(BorderStyle.THIN);
			dataStyle.setBorderRight(BorderStyle.THIN);

			// Write data rows
			int rowNum = 1;
			for (CatalogItemDTO item : items) {
				Row row = sheet.createRow(rowNum++);

				createCell(row, 0, item.getModulo(), dataStyle);
				createCell(row, 1, item.getCampo(), dataStyle);
				createCell(row, 2, item.getValor(), dataStyle);
				createCell(row, 3, item.getSbsNo() != null ? String.valueOf(item.getSbsNo()) : "", dataStyle);
				createCell(row, 4, item.getConversionDomain() != null ? item.getConversionDomain() : "", dataStyle);
				createCell(row, 5, item.getConversionStatus() != null ? String.valueOf(item.getConversionStatus()) : "1", dataStyle);
			}

			// Auto-size columns
			for (int i = 0; i < CONVERSION_HEADERS.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// Write to byte array
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);

			log.info("Exported {} items to conversions Excel template", items.size());
			return outputStream.toByteArray();
		}
	}

	/**
	 * Import conversions from CSV file
	 */
	public ImportResultDTO importFromCsv(MultipartFile file) throws IOException, CsvException {
		ImportResultDTO result = new ImportResultDTO();
		List<String> errors = new ArrayList<>();

		try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
			List<String[]> rows = reader.readAll();

			if (rows.isEmpty()) {
				result.setSuccess(false);
				result.setMessage("Empty file");
				return result;
			}

			// Skip header row
			boolean isFirstRow = true;
			int rowNumber = 0;
			int created = 0;
			int updated = 0;
			int failed = 0;

			for (String[] row : rows) {
				rowNumber++;

				if (isFirstRow) {
					isFirstRow = false;
					continue; // Skip header
				}

				if (row.length < 6) {
					errors.add("Row " + rowNumber + ": Invalid number of columns (expected 6, got " + row.length + ")");
					failed++;
					continue;
				}

				try {
					ConversionDTO dto = parseConversionRow(row);
					boolean isNew = processConversion(dto);

					if (isNew) {
						created++;
					} else {
						updated++;
					}
				} catch (Exception e) {
					errors.add("Row " + rowNumber + ": " + e.getMessage());
					failed++;
				}
			}

			result.setSuccess(failed == 0);
			result.setCreated(created);
			result.setUpdated(updated);
			result.setFailed(failed);
			result.setErrors(errors);
			result.setMessage(String.format("Import completed: %d created, %d updated, %d failed", created, updated, failed));
		}

		log.info("CSV Import result: {}", result.getMessage());
		return result;
	}

	/**
	 * Import conversions from Excel file
	 */
	public ImportResultDTO importFromExcel(MultipartFile file) throws IOException {
		ImportResultDTO result = new ImportResultDTO();
		List<String> errors = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);

			if (sheet.getPhysicalNumberOfRows() == 0) {
				result.setSuccess(false);
				result.setMessage("Empty file");
				return result;
			}

			int created = 0;
			int updated = 0;
			int failed = 0;

			// Skip header row (row 0)
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);

				if (row == null) {
					continue;
				}

				try {
					ConversionDTO dto = parseExcelRow(row);
					boolean isNew = processConversion(dto);

					if (isNew) {
						created++;
					} else {
						updated++;
					}
				} catch (Exception e) {
					errors.add("Row " + (rowNum + 1) + ": " + e.getMessage());
					failed++;
				}
			}

			result.setSuccess(failed == 0);
			result.setCreated(created);
			result.setUpdated(updated);
			result.setFailed(failed);
			result.setErrors(errors);
			result.setMessage(String.format("Import completed: %d created, %d updated, %d failed", created, updated, failed));
		}

		log.info("Excel Import result: {}", result.getMessage());
		return result;
	}

	/**
	 * Parse a CSV row into ConversionDTO
	 */
	private ConversionDTO parseConversionRow(String[] row) {
		ConversionDTO dto = new ConversionDTO();
		dto.setModulo(row[0].trim());
		dto.setCampo(row[1].trim());
		dto.setValor(row[2].trim());
		dto.setCadena(parseInteger(row[3].trim(), "Cadena"));
		dto.setDomain(row[4].trim().isEmpty() ? null : row[4].trim());
		dto.setStatus(row[5].trim().isEmpty() ? 1 : parseInteger(row[5].trim(), "Status"));

		validateConversionDTO(dto);
		return dto;
	}

	/**
	 * Parse an Excel row into ConversionDTO
	 */
	private ConversionDTO parseExcelRow(Row row) {
		ConversionDTO dto = new ConversionDTO();
		dto.setModulo(getCellStringValue(row.getCell(0)));
		dto.setCampo(getCellStringValue(row.getCell(1)));
		dto.setValor(getCellStringValue(row.getCell(2)));
		dto.setCadena(getCellIntValue(row.getCell(3), "Cadena"));
		dto.setDomain(getCellStringValue(row.getCell(4)));
		dto.setStatus(getCellIntValueOrDefault(row.getCell(5), 1));

		validateConversionDTO(dto);
		return dto;
	}

	/**
	 * Process a conversion (create or update)
	 * @return true if created, false if updated
	 */
	private boolean processConversion(ConversionDTO dto) {
		boolean exists = conversionService.getConversion(
				dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getCadena()
		).isPresent();

		if (exists) {
			conversionService.updateConversion(
					dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getCadena(), dto
			);
			return false;
		} else {
			conversionService.createConversion(dto);
			return true;
		}
	}

	/**
	 * Validate a ConversionDTO
	 */
	private void validateConversionDTO(ConversionDTO dto) {
		if (dto.getModulo() == null || dto.getModulo().isEmpty()) {
			throw new IllegalArgumentException("Modulo is required");
		}
		if (dto.getCampo() == null || dto.getCampo().isEmpty()) {
			throw new IllegalArgumentException("Campo is required");
		}
		if (dto.getValor() == null || dto.getValor().isEmpty()) {
			throw new IllegalArgumentException("Valor is required");
		}
		if (dto.getCadena() == null) {
			throw new IllegalArgumentException("Cadena is required");
		}
	}

	/**
	 * Helper method to create a cell with style
	 */
	private void createCell(Row row, int column, String value, CellStyle style) {
		Cell cell = row.createCell(column);
		cell.setCellValue(value != null ? value : "");
		cell.setCellStyle(style);
	}

	/**
	 * Helper method to parse integer
	 */
	private Integer parseInteger(String value, String fieldName) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(fieldName + " must be a valid integer: " + value);
		}
	}

	/**
	 * Helper method to get cell string value
	 */
	private String getCellStringValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue().trim();
			case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
			case BLANK -> null;
			default -> cell.toString().trim();
		};
	}

	/**
	 * Helper method to get cell integer value
	 */
	private Integer getCellIntValue(Cell cell, String fieldName) {
		if (cell == null) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> (int) cell.getNumericCellValue();
			case STRING -> parseInteger(cell.getStringCellValue().trim(), fieldName);
			default -> throw new IllegalArgumentException(fieldName + " must be a valid integer");
		};
	}

	/**
	 * Helper method to get cell integer value with default
	 */
	private Integer getCellIntValueOrDefault(Cell cell, int defaultValue) {
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return defaultValue;
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> (int) cell.getNumericCellValue();
			case STRING -> {
				String value = cell.getStringCellValue().trim();
				yield value.isEmpty() ? defaultValue : Integer.parseInt(value);
			}
			default -> defaultValue;
		};
	}

	/**
	 * Export catalog import template to CSV
	 */
	public byte[] exportCatalogImportTemplateToCsv() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
			writer.writeNext(CATALOG_IMPORT_HEADERS);
		}
		log.info("Exported catalog import template to CSV");
		return outputStream.toByteArray();
	}

	/**
	 * Export catalog import template to Excel
	 */
	public byte[] exportCatalogImportTemplateToExcel() throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Catalogs");

			// Create header style
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < CATALOG_IMPORT_HEADERS.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(CATALOG_IMPORT_HEADERS[i]);
				cell.setCellStyle(headerStyle);
			}

			// Auto-size columns
			for (int i = 0; i < CATALOG_IMPORT_HEADERS.length; i++) {
				sheet.autoSizeColumn(i);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);

			log.info("Exported catalog import template to Excel");
			return outputStream.toByteArray();
		}
	}

	/**
	 * Import catalogs (with optional conversions) from CSV file
	 */
	public ImportResultDTO importCatalogsFromCsv(MultipartFile file) throws IOException, CsvException {
		ImportResultDTO result = new ImportResultDTO();
		List<String> errors = new ArrayList<>();

		try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
			List<String[]> rows = reader.readAll();

			if (rows.isEmpty()) {
				result.setSuccess(false);
				result.setMessage("Empty file");
				return result;
			}

			boolean isFirstRow = true;
			int rowNumber = 0;
			int catalogsCreated = 0;
			int catalogsUpdated = 0;
			int conversionsCreated = 0;
			int conversionsUpdated = 0;
			int skippedConversions = 0;
			int failed = 0;

			for (String[] row : rows) {
				rowNumber++;

				if (isFirstRow) {
					isFirstRow = false;
					continue;
				}

				if (row.length < 4) {
					errors.add("Row " + rowNumber + ": Invalid number of columns (expected at least 4, got " + row.length + ")");
					failed++;
					continue;
				}

				try {
					CatalogImportRow catalogRow = parseCatalogRow(row);
					CatalogImportResult importResult = processCatalogImport(catalogRow);

					if (importResult.catalogCreated) {
						catalogsCreated++;
					} else if (importResult.catalogUpdated) {
						catalogsUpdated++;
					}

					if (importResult.conversionCreated) {
						conversionsCreated++;
					} else if (importResult.conversionUpdated) {
						conversionsUpdated++;
					} else if (importResult.conversionSkipped) {
						skippedConversions++;
					}

				} catch (Exception e) {
					errors.add("Row " + rowNumber + ": " + e.getMessage());
					failed++;
				}
			}

			result.setSuccess(failed == 0);
			result.setCatalogsCreated(catalogsCreated);
			result.setCatalogsUpdated(catalogsUpdated);
			result.setConversionsCreated(conversionsCreated);
			result.setConversionsUpdated(conversionsUpdated);
			result.setSkippedConversions(skippedConversions);
			result.setCreated(catalogsCreated + conversionsCreated);
			result.setUpdated(catalogsUpdated + conversionsUpdated);
			result.setFailed(failed);
			result.setErrors(errors);
			result.setMessage(String.format(
					"Import completed: Catalogs (%d created, %d updated), Conversions (%d created, %d updated, %d skipped), %d failed",
					catalogsCreated, catalogsUpdated, conversionsCreated, conversionsUpdated, skippedConversions, failed
			));
		}

		log.info("CSV Catalog Import result: {}", result.getMessage());
		return result;
	}

	/**
	 * Import catalogs (with optional conversions) from Excel file
	 */
	public ImportResultDTO importCatalogsFromExcel(MultipartFile file) throws IOException {
		ImportResultDTO result = new ImportResultDTO();
		List<String> errors = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);

			if (sheet.getPhysicalNumberOfRows() == 0) {
				result.setSuccess(false);
				result.setMessage("Empty file");
				return result;
			}

			int catalogsCreated = 0;
			int catalogsUpdated = 0;
			int conversionsCreated = 0;
			int conversionsUpdated = 0;
			int skippedConversions = 0;
			int failed = 0;

			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);

				if (row == null) {
					continue;
				}

				try {
					CatalogImportRow catalogRow = parseCatalogExcelRow(row);
					CatalogImportResult importResult = processCatalogImport(catalogRow);

					if (importResult.catalogCreated) {
						catalogsCreated++;
					} else if (importResult.catalogUpdated) {
						catalogsUpdated++;
					}

					if (importResult.conversionCreated) {
						conversionsCreated++;
					} else if (importResult.conversionUpdated) {
						conversionsUpdated++;
					} else if (importResult.conversionSkipped) {
						skippedConversions++;
					}

				} catch (Exception e) {
					errors.add("Row " + (rowNum + 1) + ": " + e.getMessage());
					failed++;
				}
			}

			result.setSuccess(failed == 0);
			result.setCatalogsCreated(catalogsCreated);
			result.setCatalogsUpdated(catalogsUpdated);
			result.setConversionsCreated(conversionsCreated);
			result.setConversionsUpdated(conversionsUpdated);
			result.setSkippedConversions(skippedConversions);
			result.setCreated(catalogsCreated + conversionsCreated);
			result.setUpdated(catalogsUpdated + conversionsUpdated);
			result.setFailed(failed);
			result.setErrors(errors);
			result.setMessage(String.format(
					"Import completed: Catalogs (%d created, %d updated), Conversions (%d created, %d updated, %d skipped), %d failed",
					catalogsCreated, catalogsUpdated, conversionsCreated, conversionsUpdated, skippedConversions, failed
			));
		}

		log.info("Excel Catalog Import result: {}", result.getMessage());
		return result;
	}

	/**
	 * Internal class to hold parsed catalog row data
	 */
	private record CatalogImportRow(
			String modulo,
			String campo,
			String valor,
			Integer cadena,
			String descripcion,
			Integer orden,
			String domain,
			Integer status
	) {}

	/**
	 * Internal class to hold import result for a single row
	 */
	private static class CatalogImportResult {
		boolean catalogCreated = false;
		boolean catalogUpdated = false;
		boolean conversionCreated = false;
		boolean conversionUpdated = false;
		boolean conversionSkipped = false;
	}

	/**
	 * Parse a CSV row into CatalogImportRow
	 */
	private CatalogImportRow parseCatalogRow(String[] row) {
		String modulo = row[0].trim();
		String campo = row[1].trim();
		String valor = row[2].trim();
		Integer cadena = parseInteger(row[3].trim(), "Cadena");
		String descripcion = row.length > 4 && !row[4].trim().isEmpty() ? row[4].trim() : null;
		Integer orden = row.length > 5 && !row[5].trim().isEmpty() ? parseInteger(row[5].trim(), "Orden") : null;
		String domain = row.length > 6 && !row[6].trim().isEmpty() ? row[6].trim() : null;
		Integer status = row.length > 7 && !row[7].trim().isEmpty() ? parseInteger(row[7].trim(), "Status") : 1;

		validateCatalogImportRow(modulo, campo, valor, cadena);
		return new CatalogImportRow(modulo, campo, valor, cadena, descripcion, orden, domain, status);
	}

	/**
	 * Parse an Excel row into CatalogImportRow
	 */
	private CatalogImportRow parseCatalogExcelRow(Row row) {
		String modulo = getCellStringValue(row.getCell(0));
		String campo = getCellStringValue(row.getCell(1));
		String valor = getCellStringValue(row.getCell(2));
		Integer cadena = getCellIntValue(row.getCell(3), "Cadena");
		String descripcion = getCellStringValue(row.getCell(4));
		Integer orden = getCellIntValueOrNull(row.getCell(5));
		String domain = getCellStringValue(row.getCell(6));
		Integer status = getCellIntValueOrDefault(row.getCell(7), 1);

		validateCatalogImportRow(modulo, campo, valor, cadena);
		return new CatalogImportRow(modulo, campo, valor, cadena, descripcion, orden, domain, status);
	}

	/**
	 * Validate required fields for catalog import
	 */
	private void validateCatalogImportRow(String modulo, String campo, String valor, Integer cadena) {
		if (modulo == null || modulo.isEmpty()) {
			throw new IllegalArgumentException("Modulo is required");
		}
		if (campo == null || campo.isEmpty()) {
			throw new IllegalArgumentException("Campo is required");
		}
		if (valor == null || valor.isEmpty()) {
			throw new IllegalArgumentException("Valor is required");
		}
		if (cadena == null) {
			throw new IllegalArgumentException("Cadena is required");
		}
	}

	/**
	 * Process a catalog import row - creates/updates catalog and optionally conversion
	 */
	private CatalogImportResult processCatalogImport(CatalogImportRow row) {
		CatalogImportResult result = new CatalogImportResult();

		// Try to create/update the catalog
		CatalogItemDTO catalogDTO = new CatalogItemDTO();
		catalogDTO.setModulo(row.modulo());
		catalogDTO.setCampo(row.campo());
		catalogDTO.setValor(row.valor());
		catalogDTO.setSbsNo(row.cadena());
		catalogDTO.setDescripcion(row.descripcion());
		catalogDTO.setOrden(row.orden());
		catalogDTO.setActivo(1);

		// Check if catalog exists
		boolean catalogExists = catalogService.catalogExists(row.modulo(), row.campo(), row.valor(), row.cadena());

		if (catalogExists) {
			// Catalog exists - update is not supported in bulk import to avoid accidental overwrites
			result.catalogUpdated = true;
		} else {
			// Create new catalog
			try {
				catalogService.createLegacyCatalog(catalogDTO);
				result.catalogCreated = true;
			} catch (IllegalStateException e) {
				// Catalog exists in RPRO, treat as "updated" (no action taken)
				result.catalogUpdated = true;
			}
		}

		// Process conversion if domain is provided
		if (row.domain() != null && !row.domain().isEmpty()) {
			ConversionDTO conversionDTO = new ConversionDTO();
			conversionDTO.setModulo(row.modulo());
			conversionDTO.setCampo(row.campo());
			conversionDTO.setValor(row.valor());
			conversionDTO.setCadena(row.cadena());
			conversionDTO.setDomain(row.domain());
			conversionDTO.setStatus(row.status());

			boolean conversionExists = conversionService.getConversion(
					row.modulo(), row.campo(), row.valor(), row.cadena()
			).isPresent();

			if (conversionExists) {
				conversionService.updateConversion(
						row.modulo(), row.campo(), row.valor(), row.cadena(), conversionDTO
				);
				result.conversionUpdated = true;
			} else {
				conversionService.createConversion(conversionDTO);
				result.conversionCreated = true;
			}
		} else {
			result.conversionSkipped = true;
		}

		return result;
	}

	/**
	 * Helper method to get cell integer value or null
	 */
	private Integer getCellIntValueOrNull(Cell cell) {
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return null;
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> (int) cell.getNumericCellValue();
			case STRING -> {
				String value = cell.getStringCellValue().trim();
				yield value.isEmpty() ? null : Integer.parseInt(value);
			}
			default -> null;
		};
	}
}
