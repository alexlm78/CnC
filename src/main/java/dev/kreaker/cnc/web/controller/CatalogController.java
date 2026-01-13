package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.domain.model.CatalogSource;
import dev.kreaker.cnc.service.CatalogService;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.CatalogItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
			Model model) {

		log.debug("Listing catalogs with filter: {}", filter);

		// Set default to GNC (1) if no filter is provided
		if (filter.getSbsNo() == null) {
			filter.setSbsNo(1);
		}

		List<CatalogItemDTO> catalogs = catalogService.getUnifiedCatalog(filter);

		// Get sorted modulos and campos
		List<String> modulos = catalogService.getDistinctModulos().stream()
				.sorted()
				.collect(Collectors.toList());

		List<String> campos = catalogService.getDistinctCampos().stream()
				.sorted()
				.collect(Collectors.toList());

		model.addAttribute("catalogs", catalogs);
		model.addAttribute("filter", filter);
		model.addAttribute("modulos", modulos);
		model.addAttribute("campos", campos);
		model.addAttribute("sbsNos", catalogService.getDistinctSbsNos());
		model.addAttribute("moduloCamposMap", catalogService.getCamposByModulo());

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
