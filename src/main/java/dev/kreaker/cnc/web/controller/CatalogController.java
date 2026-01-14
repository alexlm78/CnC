package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.domain.model.CatalogSource;
import dev.kreaker.cnc.service.CatalogService;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.CatalogItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/catalogs")
@Slf4j
@RequiredArgsConstructor
public class CatalogController {

	private final CatalogService catalogService;

	@GetMapping
	public String listCatalogs(
			@ModelAttribute CatalogFilterDTO filter,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			Model model) {

		log.debug("Listing catalogs with filter: {}, page: {}, size: {}", filter, page, size);

		// Set default to GNC (1) if no filter is provided
		if (filter.getSbsNo() == null) {
			filter.setSbsNo(1);
		}

		// Create Pageable object
		Pageable pageable = PageRequest.of(page, size);

		// Get paginated catalogs
		Page<CatalogItemDTO> catalogPage = catalogService.getUnifiedCatalogPage(filter, pageable);

		// Get sorted modulos and campos
		List<String> modulos = catalogService.getDistinctModulos().stream()
				.sorted()
				.collect(Collectors.toList());

		List<String> campos = catalogService.getDistinctCampos().stream()
				.sorted()
				.collect(Collectors.toList());

		model.addAttribute("catalogPage", catalogPage);
		model.addAttribute("catalogs", catalogPage.getContent());
		model.addAttribute("filter", filter);
		model.addAttribute("modulos", modulos);
		model.addAttribute("campos", campos);
		model.addAttribute("sbsNos", catalogService.getDistinctSbsNos());
		model.addAttribute("moduloCamposMap", catalogService.getCamposByModulo());
		model.addAttribute("currentPage", page);
		model.addAttribute("pageSize", size);

		return "catalog/list";
	}

	@GetMapping("/{source}/{id}")
	public String viewCatalogItem(
			@PathVariable("source") CatalogSource source,
			@PathVariable("id") Long id,
			Model model) {

		CatalogItemDTO item = catalogService.getCatalogItem(source, id)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Catalog item not found"
				));

		model.addAttribute("item", item);
		return "catalog/detail";
	}
}
