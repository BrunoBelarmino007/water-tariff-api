package com.gruporas.watertariffapi.repository;

import com.gruporas.watertariffapi.model.TabelaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface TabelaTarifariaRepository extends JpaRepository<TabelaTarifaria, Long> {

    // Busca todas as tabelas ativas
    List<TabelaTarifaria> findAllByAtivaTrue();

    // Busca a tabela ativa mais recente (por data de vigencia e criacao)
    @Query("SELECT t FROM TabelaTarifaria t WHERE t.ativa = true ORDER BY t.dataVigencia DESC, t.createdAt DESC LIMIT 1")
    Optional<TabelaTarifaria> findLatestAtiva();
    
}
