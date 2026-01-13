package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.service.ConversionService;
import dev.kreaker.cnc.service.dto.ConversionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/conversions")
@Slf4j
@RequiredArgsConstructor
public class ConversionController {

	private final ConversionService conversionService;

	@GetMapping("/new")
	public String showCreateForm(
			@RequestParam("modulo") String modulo,
			@RequestParam("campo") String campo,
			@RequestParam("valor") String valor,
			@RequestParam("cadena") Integer cadena,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			Model model) {

		ConversionDTO dto = new ConversionDTO();
		dto.setModulo(modulo);
		dto.setCampo(campo);
		dto.setValor(valor);
		dto.setCadena(cadena);

		// Determine catalog source for information display
		String catalogSource = conversionService.determineCatalogSource(modulo, campo, valor, cadena);
		dto.setCatalogSource(catalogSource);

		model.addAttribute("conversion", dto);
		model.addAttribute("isNew", true);
		model.addAttribute("returnModulo", returnModulo);
		model.addAttribute("returnCampo", returnCampo);
		model.addAttribute("returnSbsNo", returnSbsNo);
		model.addAttribute("returnHasConversion", returnHasConversion);

		return "conversion/form";
	}

	@PostMapping
	public String createConversion(
			@Valid @ModelAttribute("conversion") ConversionDTO dto,
			BindingResult result,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("isNew", true);
			model.addAttribute("returnModulo", returnModulo);
			model.addAttribute("returnCampo", returnCampo);
			model.addAttribute("returnSbsNo", returnSbsNo);
			model.addAttribute("returnHasConversion", returnHasConversion);
			return "conversion/form";
		}

		try {
			conversionService.createConversion(dto);
			redirectAttributes.addFlashAttribute("success", "Conversion created successfully");
		} catch (Exception e) {
			log.error("Error creating conversion", e);
			redirectAttributes.addFlashAttribute("error", "Error creating conversion: " + e.getMessage());
		}

		return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion);
	}

	@GetMapping("/edit")
	public String showEditForm(
			@RequestParam("modulo") String modulo,
			@RequestParam("campo") String campo,
			@RequestParam("valor") String valor,
			@RequestParam("cadena") Integer cadena,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			Model model) {

		ConversionDTO dto = conversionService.getConversion(modulo, campo, valor, cadena)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Conversion not found"
				));

		model.addAttribute("conversion", dto);
		model.addAttribute("isNew", false);
		model.addAttribute("returnModulo", returnModulo);
		model.addAttribute("returnCampo", returnCampo);
		model.addAttribute("returnSbsNo", returnSbsNo);
		model.addAttribute("returnHasConversion", returnHasConversion);

		return "conversion/form";
	}

	@PostMapping("/update")
	public String updateConversion(
			@Valid @ModelAttribute("conversion") ConversionDTO dto,
			BindingResult result,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("isNew", false);
			model.addAttribute("returnModulo", returnModulo);
			model.addAttribute("returnCampo", returnCampo);
			model.addAttribute("returnSbsNo", returnSbsNo);
			model.addAttribute("returnHasConversion", returnHasConversion);
			return "conversion/form";
		}

		try {
			// Use the values from the DTO itself (they are in the composite key)
			conversionService.updateConversion(dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getCadena(), dto);
			redirectAttributes.addFlashAttribute("success", "Conversion updated successfully");
		} catch (Exception e) {
			log.error("Error updating conversion", e);
			redirectAttributes.addFlashAttribute("error", "Error updating conversion: " + e.getMessage());
		}

		return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion);
	}

	@PostMapping("/delete")
	public String deleteConversion(
			@RequestParam("modulo") String modulo,
			@RequestParam("campo") String campo,
			@RequestParam("valor") String valor,
			@RequestParam("cadena") Integer cadena,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			RedirectAttributes redirectAttributes) {

		try {
			conversionService.deleteConversion(modulo, campo, valor, cadena);
			redirectAttributes.addFlashAttribute("success", "Conversion deleted successfully");
		} catch (Exception e) {
			log.error("Error deleting conversion", e);
			redirectAttributes.addFlashAttribute("error", "Error deleting conversion: " + e.getMessage());
		}

		return buildRedirectUrl(returnModulo, returnCampo, returnSbsNo, returnHasConversion);
	}

	@GetMapping("/view")
	public String viewConversion(
			@RequestParam("modulo") String modulo,
			@RequestParam("campo") String campo,
			@RequestParam("valor") String valor,
			@RequestParam("cadena") Integer cadena,
			@RequestParam(value = "returnModulo", required = false) String returnModulo,
			@RequestParam(value = "returnCampo", required = false) String returnCampo,
			@RequestParam(value = "returnSbsNo", required = false) Integer returnSbsNo,
			@RequestParam(value = "returnHasConversion", required = false) Boolean returnHasConversion,
			Model model) {

		ConversionDTO dto = conversionService.getConversion(modulo, campo, valor, cadena)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Conversion not found"
				));

		model.addAttribute("conversion", dto);
		model.addAttribute("returnModulo", returnModulo);
		model.addAttribute("returnCampo", returnCampo);
		model.addAttribute("returnSbsNo", returnSbsNo);
		model.addAttribute("returnHasConversion", returnHasConversion);

		return "conversion/detail";
	}

	private String buildRedirectUrl(String returnModulo, String returnCampo, Integer returnSbsNo, Boolean returnHasConversion) {
		StringBuilder url = new StringBuilder("redirect:/catalogs");
		List<String> params = new ArrayList<>();

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

		if (!params.isEmpty()) {
			url.append("?").append(String.join("&", params));
		}

		return url.toString();
	}
}
