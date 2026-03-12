/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.kreaker.cnc.domain.entity.AlCatalogTargets;
import dev.kreaker.cnc.domain.entity.AlCatalogTwostepId;

@Repository
public interface AlCatalogTargetsRepository
         extends JpaRepository<AlCatalogTargets, AlCatalogTwostepId> {

   Optional<AlCatalogTargets> findById_ModuloAndId_CampoAndId_ValorAndId_Cadena(String modulo,
            String campo, String valor, Integer cadena);
}
