package com.gruporas.watertariffapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TabelaTarifariaResponse {

    private Long id;
    private String nome;
    private LocalDate dataVigencia;
    private Boolean ativa;
    private LocalDateTime createdAt;
    private List<CategoriaResponse> categorias;

}
