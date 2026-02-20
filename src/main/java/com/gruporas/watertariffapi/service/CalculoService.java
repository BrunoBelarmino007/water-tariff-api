package com.gruporas.watertariffapi.service;

import com.gruporas.watertariffapi.dto.CalculoRequest;
import com.gruporas.watertariffapi.dto.CalculoResponse;
import com.gruporas.watertariffapi.dto.DetalhamentoFaixaResponse;
import com.gruporas.watertariffapi.exception.BusinessException;
import com.gruporas.watertariffapi.exception.ResourceNotFoundException;
import com.gruporas.watertariffapi.model.*;
import com.gruporas.watertariffapi.repository.CategoriaTarifaRepository;
import com.gruporas.watertariffapi.repository.TabelaTarifariaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CalculoService {

    private final TabelaTarifariaRepository tabelaTarifariaRepository;
    private final CategoriaTarifaRepository categoriaTarifaRepository;

    // Calcula o valor a pagar com base na categoria e consumo informados. 
    @Transactional(readOnly = true)
    
    public CalculoResponse calcular(CalculoRequest request) {
        
        // 1. Validar categoria
        CategoriaEnum categoriaEnum;
        try {
            categoriaEnum = CategoriaEnum.valueOf(
                request.getCategoria().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                "Categoria invalida: '" + request.getCategoria()
                + "'. Valores aceitos: COMERCIAL, INDUSTRIAL, PARTICULAR, PUBLICO");
        }

        // 2. Buscar tabela tarifaria ativa mais recente
        TabelaTarifaria tabela = tabelaTarifariaRepository.findLatestAtiva()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Nenhuma tabela tarifaria ativa encontrada no sistema"));

        // 3. Buscar a categoria dentro da tabela
        CategoriaTarifa categoriaTarifa = categoriaTarifaRepository
                .findByTabelaTarifariaIdAndCategoria(tabela.getId(), categoriaEnum)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Categoria '" + categoriaEnum.name()
                    + "' nao encontrada na tabela tarifaria ativa (ID: "
                    + tabela.getId() + ")"));

        // 4. Buscar faixas ordenadas por inicio
        List<FaixaConsumo> faixas = categoriaTarifa.getFaixas().stream()
                .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
                .toList();

        if (faixas.isEmpty()) {
            throw new BusinessException(
                "Nenhuma faixa de consumo cadastrada para a categoria '"
                + categoriaEnum.name() + "'");
        }

        // 5. Calcular progressivamente por faixas
        int consumo = request.getConsumo();
        int consumoRestante = consumo;
        BigDecimal valorTotal = BigDecimal.ZERO;
        List<DetalhamentoFaixaResponse> detalhamento = new ArrayList<>();

        for (FaixaConsumo faixa : faixas) {
            if (consumoRestante <= 0) break;

            int capacidade = faixa.getInicio() == 0 
                ? faixa.getFim() 
                : faixa.getFim() - faixa.getInicio() + 1;

            int m3Cobrados = Math.min(consumoRestante, capacidade);
            BigDecimal subtotal = faixa.getValorUnitario()
                .multiply(BigDecimal.valueOf(m3Cobrados));

            detalhamento.add(DetalhamentoFaixaResponse.builder()
                .faixa(DetalhamentoFaixaResponse.FaixaInfo.builder()
                    .inicio(faixa.getInicio())
                    .fim(faixa.getFim())
                    .build())
                .m3Cobrados(m3Cobrados)
                .valorUnitario(faixa.getValorUnitario())
                .subtotal(subtotal)
                .build());

            valorTotal = valorTotal.add(subtotal);
            consumoRestante -= m3Cobrados;
        }

        // Verifica se consumo excede última faixa
        if (consumoRestante > 0) {
            int faixaMaxima = faixas.get(faixas.size() - 1).getFim();
            throw new BusinessException(
                "Consumo de " + consumo + "m³ excede a faixa máxima cadastrada ("
                + faixaMaxima + "m³)");
        }

        // 6. Montar resposta
        return CalculoResponse.builder()
                .categoria(categoriaEnum.name())
                .consumoTotal(consumo)
                .valorTotal(valorTotal)
                .detalhamento(detalhamento)
                .build();
    }
}
