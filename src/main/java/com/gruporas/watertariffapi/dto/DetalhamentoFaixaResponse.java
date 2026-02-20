package com.gruporas.watertariffapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DetalhamentoFaixaResponse {

    private FaixaInfo faixa;
    private Integer m3Cobrados;
    private BigDecimal valorUnitario;
    private BigDecimal subtotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

    public static class FaixaInfo {

        private Integer inicio;
        private Integer fim;
    
    }

}
