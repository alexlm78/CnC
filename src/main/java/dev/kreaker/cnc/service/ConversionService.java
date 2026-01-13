package dev.kreaker.cnc.service;

import dev.kreaker.cnc.domain.entity.AlCatalogTwostep;
import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;
import dev.kreaker.cnc.domain.repository.AlCatalogTwostepRepository;
import dev.kreaker.cnc.domain.repository.RvCatalogosRepository;
import dev.kreaker.cnc.domain.repository.RvRproCatalogoRepository;
import dev.kreaker.cnc.service.dto.ConversionDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConversionService {

	private final AlCatalogTwostepRepository conversionRepository;
	private final RvCatalogosRepository catalogosRepository;
	private final RvRproCatalogoRepository rproCatalogoRepository;

	public List<ConversionDTO> getAllConversions() {
		return conversionRepository.findAll().stream()
				.map(ConversionDTO::fromEntity)
				.collect(Collectors.toList());
	}

	public Optional<ConversionDTO> getConversion(String modulo, String campo, String valor, Integer cadena) {
		Optional<ConversionDTO> conversionOpt = conversionRepository
				.findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(modulo, campo, valor, cadena)
				.map(ConversionDTO::fromEntity);

		// If conversion exists, determine the catalog source
		if (conversionOpt.isPresent()) {
			ConversionDTO dto = conversionOpt.get();
			dto.setCatalogSource(determineCatalogSource(modulo, campo, valor, cadena));
			return Optional.of(dto);
		}

		return conversionOpt;
	}

	@Transactional
	public ConversionDTO createConversion(ConversionDTO dto) {
		validateCatalogItemExists(dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getCadena());

		// Use findById instead of existsById to avoid Oracle 11g compatibility issues
		Optional<AlCatalogTwostep> existing = conversionRepository
				.findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(
						dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getCadena());

		if (existing.isPresent()) {
			throw new IllegalStateException("Conversion already exists for this catalog item");
		}

		AlCatalogTwostep entity = dto.toEntity();
		AlCatalogTwostep saved = conversionRepository.save(entity);
		log.info("Created conversion: {}", saved.getId());

		return ConversionDTO.fromEntity(saved);
	}

	@Transactional
	public ConversionDTO updateConversion(String modulo, String campo, String valor, Integer cadena, ConversionDTO dto) {
		AlCatalogTwostep existing = conversionRepository
				.findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(modulo, campo, valor, cadena)
				.orElseThrow(() -> new EntityNotFoundException("Conversion not found"));

		existing.setDomain(dto.getDomain());
		existing.setStatus(dto.getStatus());

		AlCatalogTwostep updated = conversionRepository.save(existing);
		log.info("Updated conversion: {}", updated.getId());

		return ConversionDTO.fromEntity(updated);
	}

	@Transactional
	public void deleteConversion(String modulo, String campo, String valor, Integer cadena) {
		AlCatalogTwostepId id = new AlCatalogTwostepId(modulo, campo, valor, cadena);
		if (!conversionRepository.existsById(id)) {
			throw new EntityNotFoundException("Conversion not found");
		}

		conversionRepository.deleteById(id);
		log.info("Deleted conversion: {}", id);
	}

	public boolean conversionExists(String modulo, String campo, String valor, Integer cadena) {
		// Use findById instead of existsById to avoid Oracle 11g compatibility issues
		return conversionRepository.findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(modulo, campo, valor, cadena)
				.isPresent();
	}

	private void validateCatalogItemExists(String modulo, String campo, String valor, Integer cadena) {
		boolean existsInLegacy = catalogosRepository.findAll().stream()
				.anyMatch(c -> c.getModulo().equals(modulo) &&
						c.getCampo().equals(campo) &&
						c.getValor().equals(valor) &&
						c.getSbsNo().equals(cadena));

		boolean existsInRpro = rproCatalogoRepository.findAll().stream()
				.anyMatch(c -> c.getModulo().equals(modulo) &&
						c.getCampo().equals(campo) &&
						c.getValor().equals(valor) &&
						c.getSbsNo().equals(cadena));

		if (!existsInLegacy && !existsInRpro) {
			throw new IllegalArgumentException(
					"Catalog item not found: modulo=" + modulo + ", campo=" + campo + ", valor=" + valor + ", cadena=" + cadena
			);
		}
	}

	public String determineCatalogSource(String modulo, String campo, String valor, Integer cadena) {
		boolean existsInLegacy = catalogosRepository.findAll().stream()
				.anyMatch(c -> c.getModulo().equals(modulo) &&
						c.getCampo().equals(campo) &&
						c.getValor().equals(valor) &&
						c.getSbsNo().equals(cadena));

		if (existsInLegacy) {
			return "RV_CATALOGOS (Legacy)";
		}

		boolean existsInRpro = rproCatalogoRepository.findAll().stream()
				.anyMatch(c -> c.getModulo().equals(modulo) &&
						c.getCampo().equals(campo) &&
						c.getValor().equals(valor) &&
						c.getSbsNo().equals(cadena));

		if (existsInRpro) {
			return "RV_RPRO_CATALOGO (RPRO)";
		}

		return "Unknown";
	}
}
