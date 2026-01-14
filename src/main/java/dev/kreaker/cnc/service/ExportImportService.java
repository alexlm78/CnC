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
}
