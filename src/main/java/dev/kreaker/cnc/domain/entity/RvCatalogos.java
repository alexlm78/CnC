package dev.kreaker.cnc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "RV_CATALOGOS", schema = "REPORTUSER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RvCatalogos {

	@Id
	@Column(name = "P_ID")
	private Long id;

	@Column(name = "SBS_NO", nullable = false)
	private Integer sbsNo;

	@Column(name = "MODULO", nullable = false, length = 50)
	private String modulo;

	@Column(name = "CAMPO", nullable = false, length = 50)
	private String campo;

	@Column(name = "VALOR", nullable = false, length = 100)
	private String valor;

	@Column(name = "DESCRIPCION", length = 200)
	private String descripcion;

	@Column(name = "ACTIVO", nullable = false)
	private Integer activo;

	@Column(name = "ORDEN")
	private Integer orden;

	@Column(name = "CREADO_POR", nullable = false, length = 30)
	private String creadoPor;

	@Column(name = "FECHA_CREACION", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "MODIFICADO_POR", length = 30)
	private String modificadoPor;

	@Column(name = "FECHA_MODIFICACION")
	private LocalDateTime fechaModificacion;

	@Column(name = "ESTADO", length = 100)
	private String estado;
}
