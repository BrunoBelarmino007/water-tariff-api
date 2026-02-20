package com.gruporas.watertariffapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CalculoRequest {

    @NotBlank(message = "O campo 'categoria' e obrigatorio")
    private String categoria;

    @NotNull(message = "O campo 'consumo' e obrigatorio")
    @Min(value = 0, message = "O consumo deve ser maior ou igual a 0")
    private Integer consumo;
}
