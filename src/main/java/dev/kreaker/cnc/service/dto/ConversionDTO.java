package dev.kreaker.cnc.service.dto;

import dev.kreaker.cnc.domain.entity.AlCatalogTwostep;
import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionDTO {

	@NotBlank(message = "MODULO is required")
	@Size(max = 50, message = "MODULO must not exceed 50 characters")
	private String modulo;

	@NotBlank(message = "CAMPO is required")
	@Size(max = 50, message = "CAMPO must not exceed 50 characters")
	private String campo;

	@NotBlank(message = "VALOR is required")
	@Size(max = 50, message = "VALOR must not exceed 50 characters")
	private String valor;

	@NotNull(message = "CADENA (SBS_NO) is required")
	private Integer cadena;

	@Size(max = 20, message = "DOMAIN must not exceed 20 characters")
	private String domain;

	private Integer status;

	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime modifiedAt;
	private String modifiedBy;

	private String catalogDescripcion;
	private String catalogSource;  // "RV_CATALOGOS" or "RV_RPRO_CATALOGO"

	public AlCatalogTwostepId toId() {
		return new AlCatalogTwostepId(modulo, campo, valor, cadena);
	}

	public static ConversionDTO fromEntity(AlCatalogTwostep entity) {
		if (entity == null || entity.getId() == null) {
			return null;
		}
		return ConversionDTO.builder()
				.modulo(entity.getId().getModulo())
				.campo(entity.getId().getCampo())
				.valor(entity.getId().getValor())
				.cadena(entity.getId().getCadena())
				.domain(entity.getDomain())
				.status(entity.getStatus())
				.createdAt(entity.getCreatedAt())
				.createdBy(entity.getCreatedBy())
				.modifiedAt(entity.getModifiedAt())
				.modifiedBy(entity.getModifiedBy())
				.build();
	}

	public AlCatalogTwostep toEntity() {
		AlCatalogTwostep entity = new AlCatalogTwostep();
		entity.setId(new AlCatalogTwostepId(modulo, campo, valor, cadena));
		entity.setDomain(domain);
		entity.setStatus(status);
		return entity;
	}

	public String getCadenaDisplay() {
		if (cadena == null) {
			return "";
		}
		return switch (cadena) {
			case 1 -> "GNC";
			case 2 -> "Arca";
			default -> String.valueOf(cadena);
		};
	}
}
