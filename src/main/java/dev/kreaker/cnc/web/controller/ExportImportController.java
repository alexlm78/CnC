package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.service.ExportImportService;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.ImportResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/export-import")
@Slf4j
@RequiredArgsConstructor
public class ExportImportController {

	private final ExportImportService exportImportService;

	private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	/**
	 * Export catalog to CSV
	 */
	@GetMapping("/export/csv")
	public ResponseEntity<byte[]> exportToCsv(@ModelAttribute CatalogFilterDTO filter) {
		try {
			byte[] data = exportImportService.exportToCsv(filter);
			String filename = "catalog_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".csv";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.parseMediaType("text/csv"))
					.body(data);

		} catch (IOException e) {
			log.error("Error exporting to CSV", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Export catalog to Excel
	 */
	@GetMapping("/export/excel")
	public ResponseEntity<byte[]> exportToExcel(@ModelAttribute CatalogFilterDTO filter) {
		try {
			byte[] data = exportImportService.exportToExcel(filter);
			String filename = "catalog_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(data);

		} catch (IOException e) {
			log.error("Error exporting to Excel", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Export conversions template to CSV (for import)
	 */
	@GetMapping("/export/conversions/csv")
	public ResponseEntity<byte[]> exportConversionsToCsv(@ModelAttribute CatalogFilterDTO filter) {
		try {
			byte[] data = exportImportService.exportConversionsToCsv(filter);
			String filename = "conversions_template_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".csv";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.parseMediaType("text/csv"))
					.body(data);

		} catch (IOException e) {
			log.error("Error exporting conversions to CSV", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Export conversions template to Excel (for import)
	 */
	@GetMapping("/export/conversions/excel")
	public ResponseEntity<byte[]> exportConversionsToExcel(@ModelAttribute CatalogFilterDTO filter) {
		try {
			byte[] data = exportImportService.exportConversionsToExcel(filter);
			String filename = "conversions_template_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(data);

		} catch (IOException e) {
			log.error("Error exporting conversions to Excel", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Import conversions from file (CSV or Excel)
	 */
	@PostMapping("/import")
	public String importConversions(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			@RequestParam(value = "returnPage", required = false, defaultValue = "0") Integer returnPage,
			@RequestParam(value = "returnSize", required = false, defaultValue = "10") Integer returnSize,
			@RequestParam(value = "returnSearchTerm", required = false) String returnSearchTerm,
			RedirectAttributes redirectAttributes) {

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Please select a file to import");
			return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion, returnPage, returnSize, returnSearchTerm);
		}

		String filename = file.getOriginalFilename();
		if (filename == null) {
			redirectAttributes.addFlashAttribute("error", "Invalid file");
			return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion, returnPage, returnSize, returnSearchTerm);
		}

		try {
			ImportResultDTO result;

			if (filename.endsWith(".csv")) {
				result = exportImportService.importFromCsv(file);
			} else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
				result = exportImportService.importFromExcel(file);
			} else {
				redirectAttributes.addFlashAttribute("error", "Unsupported file format. Please use CSV or Excel (.xlsx)");
				return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion, returnPage, returnSize, returnSearchTerm);
			}

			if (result.isSuccess()) {
				redirectAttributes.addFlashAttribute("success", result.getMessage());
			} else {
				redirectAttributes.addFlashAttribute("warning", result.getMessage());
			}

			if (result.hasErrors()) {
				redirectAttributes.addFlashAttribute("importErrors", result.getErrors());
			}

		} catch (Exception e) {
			log.error("Error importing file", e);
			redirectAttributes.addFlashAttribute("error", "Error importing file: " + e.getMessage());
		}

		return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion, returnPage, returnSize, returnSearchTerm);
	}

	/**
	 * Build redirect URL preserving filters
	 */
	private String buildRedirectUrl(String returnModulo, String returnCampo, Integer returnSbsNo,
									Boolean returnHasConversion, Integer returnPage, Integer returnSize, String returnSearchTerm) {
		StringBuilder url = new StringBuilder("redirect:/catalogs");
		java.util.List<String> params = new java.util.ArrayList<>();

		if (returnModulo != null && !returnModulo.isEmpty()) {
			params.add("modulo=" + returnModulo);
		}
		if (returnCampo != null && !returnCampo.isEmpty()) {
			params.add("campo=" + returnCampo);
		}
		if (returnSbsNo != null) {
			params.add("sbsNo=" + returnSbsNo);
		}
		if (returnHasConversion != null) {
			params.add("hasConversion=" + returnHasConversion);
		}
		if (returnPage != null) {
			params.add("page=" + returnPage);
		}
		if (returnSize != null) {
			params.add("size=" + returnSize);
		}
		if (returnSearchTerm != null && !returnSearchTerm.isEmpty()) {
			params.add("searchTerm=" + returnSearchTerm);
		}

		if (!params.isEmpty()) {
			url.append("?").append(String.join("&", params));
		}

		return url.toString();
	}
}
