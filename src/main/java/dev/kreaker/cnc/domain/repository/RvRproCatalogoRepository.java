package dev.kreaker.cnc.domain.repository;

import dev.kreaker.cnc.domain.entity.RvRproCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RvRproCatalogoRepository extends JpaRepository<RvRproCatalogo, Long> {

	List<RvRproCatalogo> findByActivoOrderByOrden(Integer activo);

	List<RvRproCatalogo> findByActivoAndModuloOrderByOrden(Integer activo, String modulo);

	List<RvRproCatalogo> findByActivoAndCampoOrderByOrden(Integer activo, String campo);

	@Query("SELECT c FROM RvRproCatalogo c WHERE c.activo = 1 " +
			"AND (:modulo IS NULL OR c.modulo = :modulo) " +
			"AND (:campo IS NULL OR c.campo = :campo) " +
			"AND (:sbsNo IS NULL OR c.sbsNo = :sbsNo) " +
			"ORDER BY c.orden")
	List<RvRproCatalogo> findActiveWithFilters(
			@Param("modulo") String modulo,
			@Param("campo") String campo,
			@Param("sbsNo") Integer sbsNo
	);
}
