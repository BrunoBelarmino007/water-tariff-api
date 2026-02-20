package com.gruporas.watertariffapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CalculoResponse {

    private String categoria;
    private Integer consumoTotal;
    private BigDecimal valorTotal;
    private List<DetalhamentoFaixaResponse> detalhamento;
 
}
