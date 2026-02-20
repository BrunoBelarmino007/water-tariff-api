package com.gruporas.watertariffapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TabelaTarifariaRequest {

    @NotBlank(message = "O campo 'nome' e obrigatorio")
    private String nome;

    @NotNull(message = "O campo 'dataVigencia' e obrigatorio")
    private LocalDate dataVigencia;

    @NotEmpty(message = "A lista de categorias nao pode estar vazia")
    @Valid
    private List<CategoriaRequest> categorias;
}
