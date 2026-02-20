package com.gruporas.watertariffapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class FaixaRequest {

    @NotNull(message = "O campo 'inicio' e obrigatorio")
    @Min(value = 0, message = "O campo 'inicio' deve ser maior ou igual a 0")
    private Integer inicio;

    @NotNull(message = "O campo 'fim' e obrigatorio")
    @Min(value = 1, message = "O campo 'fim' deve ser maior ou igual a 1")
    private Integer fim;

    @NotNull(message = "O campo 'valorUnitario' e obrigatorio")
    @DecimalMin(value = "0.01", message = "O valor unitario deve ser maior que zero")
    private BigDecimal valorUnitario;
}
