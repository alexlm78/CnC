/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.kreaker.cnc.domain.entity.RvCatalogos;

@Repository
public interface RvCatalogosRepository extends JpaRepository<RvCatalogos, Long> {

   List<RvCatalogos> findByActivoOrderByOrden(Integer activo);

   List<RvCatalogos> findByActivoAndModuloOrderByOrden(Integer activo, String modulo);

   List<RvCatalogos> findByActivoAndCampoOrderByOrden(Integer activo, String campo);

   @Query("SELECT c FROM RvCatalogos c WHERE c.activo = 1 "
            + "AND (:modulo IS NULL OR c.modulo = :modulo) "
            + "AND (:campo IS NULL OR c.campo = :campo) "
            + "AND (:sbsNo IS NULL OR c.sbsNo = :sbsNo) " + "ORDER BY c.orden")
   List<RvCatalogos> findActiveWithFilters(@Param("modulo") String modulo,
            @Param("campo") String campo, @Param("sbsNo") Integer sbsNo);
}
