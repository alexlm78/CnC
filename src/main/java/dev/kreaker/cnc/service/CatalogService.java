package dev.kreaker.cnc.service;

import dev.kreaker.cnc.domain.entity.AlCatalogTwostep;
import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;
import dev.kreaker.cnc.domain.entity.RvCatalogos;
import dev.kreaker.cnc.domain.entity.RvRproCatalogo;
import dev.kreaker.cnc.domain.model.CatalogSource;
import dev.kreaker.cnc.domain.repository.AlCatalogTwostepRepository;
import dev.kreaker.cnc.domain.repository.RvCatalogosRepository;
import dev.kreaker.cnc.domain.repository.RvRproCatalogoRepository;
import dev.kreaker.cnc.service.dto.CatalogFilterDTO;
import dev.kreaker.cnc.service.dto.CatalogItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
	@RequiredArgsConstructor
	public class CatalogService {

	private final RvCatalogosRepository catalogosRepository;
	private final RvRproCatalogoRepository rproCatalogoRepository;
	private final AlCatalogTwostepRepository conversionRepository;

	public List<CatalogItemDTO> getUnifiedCatalog(CatalogFilterDTO filter) {
		List<CatalogItemDTO> result = new ArrayList<>();

		if (filter.getSource() == null || filter.getSource() == CatalogSource.LEGACY) {
			List<RvCatalogos> legacyCatalogs = catalogosRepository.findActiveWithFilters(
					filter.getModulo(), filter.getCampo(), filter.getSbsNo()
			);
			result.addAll(mapLegacyToDTO(legacyCatalogs));
		}

		if (filter.getSource() == null || filter.getSource() == CatalogSource.RPRO) {
			List<RvRproCatalogo> rproCatalogs = rproCatalogoRepository.findActiveWithFilters(
					filter.getModulo(), filter.getCampo(), filter.getSbsNo()
			);
			result.addAll(mapRproToDTO(rproCatalogs));
		}

		enrichWithConversionData(result);

		if (filter.getHasConversion() != null) {
			result = result.stream()
					.filter(item -> item.isHasConversion() == filter.getHasConversion())
					.collect(Collectors.toList());
		}

		// Apply text search filter
		String searchTerm = filter.getSearchTermNormalized();
		if (searchTerm != null && !searchTerm.isEmpty()) {
			result = result.stream()
					.filter(item -> matchesSearchTerm(item, searchTerm))
					.collect(Collectors.toList());
		}

		result.sort(Comparator.comparing(CatalogItemDTO::getModulo, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(CatalogItemDTO::getCampo, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(CatalogItemDTO::getValor, Comparator.nullsLast(Comparator.naturalOrder())));

		return result;
	}

	private boolean matchesSearchTerm(CatalogItemDTO item, String searchTerm) {
		// Search in modulo, campo, valor, and descripcion (case-insensitive)
		return containsIgnoreCase(item.getModulo(), searchTerm) ||
				containsIgnoreCase(item.getCampo(), searchTerm) ||
				containsIgnoreCase(item.getValor(), searchTerm) ||
				containsIgnoreCase(item.getDescripcion(), searchTerm);
	}

	private boolean containsIgnoreCase(String text, String searchTerm) {
		if (text == null) {
			return false;
		}
		return text.toLowerCase().contains(searchTerm);
	}

	public Page<CatalogItemDTO> getUnifiedCatalogPage(CatalogFilterDTO filter, Pageable pageable) {
		// Get all catalogs with filters applied
		List<CatalogItemDTO> allItems = getUnifiedCatalog(filter);

		// Calculate pagination parameters
		int totalElements = allItems.size();
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), totalElements);

		// Get the sublist for the current page
		List<CatalogItemDTO> pageContent = start >= totalElements
				? Collections.emptyList()
				: allItems.subList(start, end);

		// Return a Page object
		return new PageImpl<>(pageContent, pageable, totalElements);
	}

	public Set<String> getDistinctModulos() {
		Set<String> modulos = new HashSet<>();
		modulos.addAll(catalogosRepository.findAll().stream()
				.map(RvCatalogos::getModulo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		modulos.addAll(rproCatalogoRepository.findAll().stream()
				.map(RvRproCatalogo::getModulo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		return modulos;
	}

	public Set<String> getDistinctCampos() {
		Set<String> campos = new HashSet<>();
		campos.addAll(catalogosRepository.findAll().stream()
				.map(RvCatalogos::getCampo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		campos.addAll(rproCatalogoRepository.findAll().stream()
				.map(RvRproCatalogo::getCampo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		return campos;
	}

	public Set<Integer> getDistinctSbsNos() {
		Set<Integer> sbsNos = new HashSet<>();
		sbsNos.addAll(catalogosRepository.findAll().stream()
				.map(RvCatalogos::getSbsNo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		sbsNos.addAll(rproCatalogoRepository.findAll().stream()
				.map(RvRproCatalogo::getSbsNo)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		return sbsNos;
	}

	public Map<String, List<String>> getCamposByModulo() {
		Map<String, Set<String>> tempMap = new HashMap<>();

		// Process legacy catalogs
		catalogosRepository.findAll().forEach(cat -> {
			if (cat.getModulo() != null && cat.getCampo() != null) {
				tempMap.computeIfAbsent(cat.getModulo(), _ -> new HashSet<>()).add(cat.getCampo());
			}
		});

		// Process RPRO catalogs
		rproCatalogoRepository.findAll().forEach(cat -> {
			if (cat.getModulo() != null && cat.getCampo() != null) {
				tempMap.computeIfAbsent(cat.getModulo(), _ -> new HashSet<>()).add(cat.getCampo());
			}
		});

		// Convert to sorted lists
		Map<String, List<String>> result = new HashMap<>();
		tempMap.forEach((modulo, campos) -> {
			List<String> sortedCampos = campos.stream()
					.sorted()
					.collect(Collectors.toList());
			result.put(modulo, sortedCampos);
		});

		return result;
	}

	public Optional<CatalogItemDTO> getCatalogItem(CatalogSource source, Long id) {
		if (source == CatalogSource.LEGACY) {
			return catalogosRepository.findById(id)
					.map(this::mapLegacyToDTO);
		} else {
			return rproCatalogoRepository.findById(id)
					.map(this::mapRproToDTO);
		}
	}

	public CatalogItemDTO createLegacyCatalog(CatalogItemDTO dto) {
		validateNotInRpro(dto.getModulo(), dto.getCampo(), dto.getValor(), dto.getSbsNo());

		RvCatalogos entity = new RvCatalogos();
		entity.setId(generateNextId());
		entity.setSbsNo(Objects.requireNonNullElse(dto.getSbsNo(), 1));
		entity.setModulo(dto.getModulo());
		entity.setCampo(dto.getCampo());
		entity.setValor(dto.getValor());
		entity.setDescripcion(dto.getDescripcion());
		entity.setOrden(dto.getOrden());
		Integer activo = dto.getActivo() != null ? dto.getActivo() : 1;
		entity.setActivo(activo);
		entity.setCreadoPor("SYSTEM");
		entity.setFechaCreacion(LocalDateTime.now());
		entity.setModificadoPor(null);
		entity.setFechaModificacion(null);
		entity.setEstado("PENDIENTE");

		RvCatalogos saved = catalogosRepository.save(entity);
		return mapLegacyToDTO(saved);
	}

	public CatalogItemDTO updateLegacyCatalog(Long id, CatalogItemDTO dto) {
		RvCatalogos existing = catalogosRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Legacy catalog not found with id " + id));

		validateNotInRpro(existing.getModulo(), existing.getCampo(), existing.getValor(), existing.getSbsNo());

		existing.setSbsNo(Objects.requireNonNullElse(dto.getSbsNo(), existing.getSbsNo()));
		existing.setModulo(dto.getModulo());
		existing.setCampo(dto.getCampo());
		existing.setValor(dto.getValor());
		existing.setDescripcion(dto.getDescripcion());
		existing.setOrden(dto.getOrden());
		Integer activo = dto.getActivo() != null ? dto.getActivo() : existing.getActivo();
		existing.setActivo(activo);
		existing.setModificadoPor("SYSTEM");
		existing.setFechaModificacion(LocalDateTime.now().withNano(0));
		existing.setEstado("PENDIENTE");

		RvCatalogos saved = catalogosRepository.save(existing);
		return mapLegacyToDTO(saved);
	}

	public void deleteLegacyCatalog(Long id) {
		RvCatalogos existing = catalogosRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Legacy catalog not found with id " + id));

		validateNotInRpro(existing.getModulo(), existing.getCampo(), existing.getValor(), existing.getSbsNo());

		catalogosRepository.delete(existing);
	}

	private List<CatalogItemDTO> mapLegacyToDTO(List<RvCatalogos> entities) {
		return entities.stream()
				.map(this::mapLegacyToDTO)
				.collect(Collectors.toList());
	}

	private CatalogItemDTO mapLegacyToDTO(RvCatalogos entity) {
		return CatalogItemDTO.builder()
				.source(CatalogSource.LEGACY)
				.sourceId(entity.getId())
				.sbsNo(entity.getSbsNo())
				.modulo(entity.getModulo())
				.campo(entity.getCampo())
				.valor(entity.getValor())
				.descripcion(entity.getDescripcion())
				.orden(entity.getOrden())
				.activo(entity.getActivo())
				.sourceDisplay("Legacy")
				.build();
	}

	private List<CatalogItemDTO> mapRproToDTO(List<RvRproCatalogo> entities) {
		return entities.stream()
				.map(this::mapRproToDTO)
				.collect(Collectors.toList());
	}

	private CatalogItemDTO mapRproToDTO(RvRproCatalogo entity) {
		return CatalogItemDTO.builder()
				.source(CatalogSource.RPRO)
				.sourceId(entity.getRproSid())
				.sbsNo(entity.getSbsNo())
				.modulo(entity.getModulo())
				.campo(entity.getCampo())
				.valor(entity.getValor())
				.descripcion(entity.getDescripcion())
				.orden(entity.getOrden())
				.activo(entity.getActivo())
				.padreSid(entity.getPadreSid())
				.sourceDisplay("RPRO")
				.build();
	}

	private void enrichWithConversionData(List<CatalogItemDTO> items) {
		List<AlCatalogTwostep> conversions = conversionRepository.findAll();

		// Handle potential duplicates by keeping the first occurrence
		Map<AlCatalogTwostepId, AlCatalogTwostep> conversionMap = conversions.stream()
				.collect(Collectors.toMap(
						AlCatalogTwostep::getId,
						Function.identity(),
						(existing, replacement) -> {
							log.warn("Duplicate conversion found for key: {}. Keeping the first one.", existing.getId());
							return existing; // Keep the first occurrence
						}
				));

		items.forEach(item -> {
			AlCatalogTwostepId key = item.getConversionKey();
			AlCatalogTwostep conversion = conversionMap.get(key);
			if (conversion != null) {
				item.setHasConversion(true);
				item.setConversionDomain(conversion.getDomain());
				item.setConversionStatus(conversion.getStatus());
			} else {
				item.setHasConversion(false);
			}
		});
	}

	private Long generateNextId() {
		return catalogosRepository.findAll().stream()
				.map(RvCatalogos::getId)
				.filter(Objects::nonNull)
				.max(Long::compareTo)
				.map(id -> id + 1)
				.orElse(1L);
	}

	private void validateNotInRpro(String modulo, String campo, String valor, Integer sbsNo) {
		boolean existsInRpro = rproCatalogoRepository.findAll().stream()
				.anyMatch(c -> Objects.equals(c.getModulo(), modulo)
						&& Objects.equals(c.getCampo(), campo)
						&& Objects.equals(c.getValor(), valor)
						&& Objects.equals(c.getSbsNo(), sbsNo));

		if (existsInRpro) {
			throw new IllegalStateException("Catalog item exists in RV_RPRO_CATALOGO and cannot be modified in RV_CATALOGOS");
		}
	}
}
