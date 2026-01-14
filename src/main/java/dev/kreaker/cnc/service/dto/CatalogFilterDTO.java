package dev.kreaker.cnc.service.dto;

import dev.kreaker.cnc.domain.model.CatalogSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogFilterDTO {

	private String modulo;
	private String campo;
	private Integer sbsNo;
	private CatalogSource source;
	private Boolean hasConversion;
	private String searchTerm;

	public boolean hasAnyFilter() {
		return modulo != null || campo != null || sbsNo != null ||
				source != null || hasConversion != null ||
				(searchTerm != null && !searchTerm.trim().isEmpty());
	}

	public String getSearchTermNormalized() {
		return searchTerm != null ? searchTerm.trim().toLowerCase() : null;
	}
}
