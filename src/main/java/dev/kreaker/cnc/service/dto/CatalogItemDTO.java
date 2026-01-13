package dev.kreaker.cnc.service.dto;

import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;
import dev.kreaker.cnc.domain.model.CatalogSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogItemDTO {

	private CatalogSource source;
	private Long sourceId;

	private Integer sbsNo;
	private String modulo;
	private String campo;
	private String valor;
	private String descripcion;
	private Integer orden;
	private Integer activo;

	private Long padreSid;

	private boolean hasConversion;
	private String conversionDomain;
	private Integer conversionStatus;

	private String sourceDisplay;

	public AlCatalogTwostepId getConversionKey() {
		return new AlCatalogTwostepId(modulo, campo, valor, sbsNo);
	}

	public String getCadenaDisplay() {
		if (sbsNo == null) {
			return "";
		}
		return switch (sbsNo) {
			case 1 -> "GNC";
			case 2 -> "Arca";
			default -> String.valueOf(sbsNo);
		};
	}
}
