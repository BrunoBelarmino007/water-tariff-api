package com.gruporas.watertariffapi.repository;

import com.gruporas.watertariffapi.model.CategoriaEnum;
import com.gruporas.watertariffapi.model.CategoriaTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface CategoriaTarifaRepository extends JpaRepository<CategoriaTarifa, Long> {

    // Busca categoria por tabela + tipo de categoria
    Optional<CategoriaTarifa> findByTabelaTarifariaIdAndCategoria(
        Long tabelaTarifariaId, CategoriaEnum categoria);
        
}
