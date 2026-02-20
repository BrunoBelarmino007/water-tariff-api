package com.gruporas.watertariffapi.repository;

import com.gruporas.watertariffapi.model.FaixaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface FaixaConsumoRepository extends JpaRepository<FaixaConsumo, Long> {

    // Busca faixas por categoria, ordenadas por inicio crescente
    List<FaixaConsumo> findByCategoriaTarifaIdOrderByInicioAsc(Long categoriaTarifaId);
    
}
