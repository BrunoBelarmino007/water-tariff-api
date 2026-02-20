package com.gruporas.watertariffapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CategoriaRequest {

    @NotBlank(message = "O campo 'categoria' e obrigatorio")
    private String categoria;

    @NotEmpty(message = "A lista de faixas nao pode estar vazia")
    @Valid
    private List<FaixaRequest> faixas;
}
