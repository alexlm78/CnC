/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kreaker.cnc.domain.entity.AlCatalogTwostep;
import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;

@Repository
public interface AlCatalogTwostepRepository
         extends JpaRepository<AlCatalogTwostep, AlCatalogTwostepId> {

   List<AlCatalogTwostep> findById_Modulo(String modulo);

   List<AlCatalogTwostep> findById_Campo(String campo);

   Optional<AlCatalogTwostep> findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(String modulo,
            String campo, String valor, Integer cadena);

   boolean existsById_ModuloAndId_CampoAndId_ValorAndId_Cadena(String modulo, String campo,
            String valor, Integer cadena);

   List<AlCatalogTwostep> findById_Cadena(Integer cadena);

   List<AlCatalogTwostep> findByStatus(Integer status);
}
