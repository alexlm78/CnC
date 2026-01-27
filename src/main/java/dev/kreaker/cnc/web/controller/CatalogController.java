package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.domain.model.CatalogSource;
import dev.kreaker.cnc.service.CatalogService;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.CatalogItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/catalogs")
@Slf4j
@RequiredArgsConstructor
public class CatalogController {

	private final CatalogService catalogService;

	@Value("${cnc.catalog.editing.enabled:true}")
	private boolean catalogEditingEnabled;

	@GetMapping
	public String listCatalogs(
			@ModelAttribute CatalogFilterDTO filter,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			Model model) {

		log.debug("Listing catalogs with filter: {}, page: {}, size: {}", filter, page, size);

		if (filter.getSbsNo() == null) {
			filter.setSbsNo(1);
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<CatalogItemDTO> catalogPage = catalogService.getUnifiedCatalogPage(filter, pageable);

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
		model.addAttribute("catalogEditingEnabled", catalogEditingEnabled);

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
		model.addAttribute("catalogEditingEnabled", catalogEditingEnabled);
		return "catalog/detail";
	}

	@GetMapping("/legacy/new")
	public String showCreateLegacyForm(
			@RequestParam(value = "modulo", required = false) String modulo,
			@RequestParam(value = "campo", required = false) String campo,
			@RequestParam(value = "sbsNo", required = false) Integer sbsNo,
			Model model) {

		if (!catalogEditingEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Catalog editing is disabled");
		}

		CatalogItemDTO dto = new CatalogItemDTO();
		dto.setSource(CatalogSource.LEGACY);
		dto.setSbsNo(Objects.requireNonNullElse(sbsNo, 1));
		dto.setModulo(modulo);
		dto.setCampo(campo);
		dto.setActivo(1);

		model.addAttribute("catalog", dto);
		model.addAttribute("isNew", true);
		return "catalog/form";
	}

	@PostMapping("/legacy")
	public String createLegacyCatalog(
			@ModelAttribute("catalog") CatalogItemDTO dto,
			RedirectAttributes redirectAttributes) {

		if (!catalogEditingEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Catalog editing is disabled");
		}

		try {
			catalogService.createLegacyCatalog(dto);
			redirectAttributes.addFlashAttribute("success", "Catalog created successfully");
		} catch (Exception e) {
			log.error("Error creating legacy catalog", e);
			redirectAttributes.addFlashAttribute("error", "Error creating catalog: " + e.getMessage());
		}

		return buildListRedirect(dto.getModulo(), dto.getCampo(), dto.getSbsNo());
	}

	@GetMapping("/legacy/{id}/edit")
	public String showEditLegacyForm(
			@PathVariable("id") Long id,
			Model model) {

		if (!catalogEditingEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Catalog editing is disabled");
		}

		CatalogItemDTO item = catalogService.getCatalogItem(CatalogSource.LEGACY, id)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Legacy catalog item not found"
				));

		model.addAttribute("catalog", item);
		model.addAttribute("isNew", false);
		return "catalog/form";
	}

	@PostMapping("/legacy/{id}/update")
	public String updateLegacyCatalog(
			@PathVariable("id") Long id,
			@ModelAttribute("catalog") CatalogItemDTO dto,
			RedirectAttributes redirectAttributes) {

		if (!catalogEditingEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Catalog editing is disabled");
		}

		try {
			catalogService.updateLegacyCatalog(id, dto);
			redirectAttributes.addFlashAttribute("success", "Catalog updated successfully");
		} catch (Exception e) {
			log.error("Error updating legacy catalog", e);
			redirectAttributes.addFlashAttribute("error", "Error updating catalog: " + e.getMessage());
		}

		return buildListRedirect(dto.getModulo(), dto.getCampo(), dto.getSbsNo());
	}

	@PostMapping("/legacy/{id}/delete")
	public String deleteLegacyCatalog(
			@PathVariable("id") Long id,
			RedirectAttributes redirectAttributes) {

		if (!catalogEditingEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Catalog editing is disabled");
		}

		try {
			CatalogItemDTO item = catalogService.getCatalogItem(CatalogSource.LEGACY, id)
					.orElseThrow(() -> new ResponseStatusException(
							HttpStatus.NOT_FOUND, "Legacy catalog item not found"
					));

			catalogService.deleteLegacyCatalog(id);
			redirectAttributes.addFlashAttribute("success", "Catalog deleted successfully");

			return buildListRedirect(item.getModulo(), item.getCampo(), item.getSbsNo());
		} catch (Exception e) {
			log.error("Error deleting legacy catalog", e);
			redirectAttributes.addFlashAttribute("error", "Error deleting catalog: " + e.getMessage());
			return "redirect:/catalogs";
		}
	}

	private String buildListRedirect(String modulo, String campo, Integer sbsNo) {
		StringBuilder url = new StringBuilder("redirect:/catalogs");
		boolean hasParam = false;

		if (modulo != null && !modulo.isEmpty()) {
			url.append(hasParam ? "&" : "?").append("modulo=").append(modulo);
			hasParam = true;
		}
		if (campo != null && !campo.isEmpty()) {
			url.append(hasParam ? "&" : "?").append("campo=").append(campo);
			hasParam = true;
		}
		if (sbsNo != null) {
			url.append(hasParam ? "&" : "?").append("sbsNo=").append(sbsNo);
		}

		return url.toString();
	}
}
