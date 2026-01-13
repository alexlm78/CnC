package dev.kreaker.cnc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlCatalogTwostepId implements Serializable {

	@Column(name = "MODULO", length = 50)
	private String modulo;

	@Column(name = "CAMPO", length = 50)
	private String campo;

	@Column(name = "VALOR", length = 50)
	private String valor;

	@Column(name = "CADENA")
	private Integer cadena;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlCatalogTwostepId that = (AlCatalogTwostepId) o;
		return Objects.equals(modulo, that.modulo) &&
				Objects.equals(campo, that.campo) &&
				Objects.equals(valor, that.valor) &&
				Objects.equals(cadena, that.cadena);
	}

	@Override
	public int hashCode() {
		return Objects.hash(modulo, campo, valor, cadena);
	}

	@Override
	public String toString() {
		return "AlCatalogTwostepId{" +
				"modulo='" + modulo + '\'' +
				", campo='" + campo + '\'' +
				", valor='" + valor + '\'' +
				", cadena=" + cadena +
				'}';
	}
}

