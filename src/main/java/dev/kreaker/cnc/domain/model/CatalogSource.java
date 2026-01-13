package dev.kreaker.cnc.domain.model;

public enum CatalogSource {
	LEGACY("RV_CATALOGOS"),
	RPRO("RV_RPRO_CATALOGO");

	private final String tableName;

	CatalogSource(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}
}
