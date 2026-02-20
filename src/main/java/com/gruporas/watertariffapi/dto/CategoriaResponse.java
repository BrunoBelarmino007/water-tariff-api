package com.gruporas.watertariffapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CategoriaResponse {

    private Long id;
    private String categoria;
    private List<FaixaResponse> faixas;
    
}
