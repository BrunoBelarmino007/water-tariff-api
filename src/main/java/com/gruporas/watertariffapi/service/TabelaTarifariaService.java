package com.gruporas.watertariffapi.service;

import com.gruporas.watertariffapi.dto.*;
import com.gruporas.watertariffapi.exception.BusinessException;
import com.gruporas.watertariffapi.exception.ResourceNotFoundException;
import com.gruporas.watertariffapi.model.*;
import com.gruporas.watertariffapi.repository.TabelaTarifariaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class TabelaTarifariaService {

    private final TabelaTarifariaRepository tabelaTarifariaRepository;

    // Cria uma nova tabela tarifaria completa com categorias e faixas.
    @Transactional

    public TabelaTarifariaResponse criar(TabelaTarifariaRequest request) {
        TabelaTarifaria tabela = TabelaTarifaria.builder()
                .nome(request.getNome())
                .dataVigencia(request.getDataVigencia())
                .ativa(true)
                .build();

        for (CategoriaRequest catReq : request.getCategorias()) {
            CategoriaEnum categoriaEnum = parseCategoriaEnum(catReq.getCategoria());

            // Validar faixas desta categoria
            validarFaixas(catReq.getFaixas(), catReq.getCategoria());

            CategoriaTarifa categoria = CategoriaTarifa.builder()
                    .categoria(categoriaEnum)
                    .tabelaTarifaria(tabela)
                    .build();

            for (FaixaRequest faixaReq : catReq.getFaixas()) {
                FaixaConsumo faixa = FaixaConsumo.builder()
                        .inicio(faixaReq.getInicio())
                        .fim(faixaReq.getFim())
                        .valorUnitario(faixaReq.getValorUnitario())
                        .categoriaTarifa(categoria)
                        .build();
                categoria.getFaixas().add(faixa);
            }

            tabela.getCategorias().add(categoria);
        }

        TabelaTarifaria salva = tabelaTarifariaRepository.save(tabela);
        return toResponse(salva);
    }

    // Lista todas as tabelas tarifarias.
    @Transactional(readOnly = true)

    public List<TabelaTarifariaResponse> listarTodas() {
        return tabelaTarifariaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Busca uma tabela tarifaria por ID.
    @Transactional(readOnly = true)

    public TabelaTarifariaResponse buscarPorId(Long id) {
        TabelaTarifaria tabela = tabelaTarifariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tabela tarifaria com ID " + id + " nao encontrada"));
        return toResponse(tabela);
    }

    // Lista faixas de consumo por tabela e categoria.
    @Transactional(readOnly = true)

    public List<FaixaResponse> listarFaixasPorCategoria(Long tabelaId, String categoria) {
        TabelaTarifaria tabela = tabelaTarifariaRepository.findById(tabelaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tabela não encontrada"));
        
        CategoriaEnum categoriaEnum = parseCategoriaEnum(categoria);
        
        CategoriaTarifa catTarifa = tabela.getCategorias().stream()
            .filter(c -> c.getCategoria() == categoriaEnum)
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(
                "Categoria " + categoria + " não encontrada na tabela"));
        
        return catTarifa.getFaixas().stream()
            .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
            .map(faixa -> FaixaResponse.builder()
                .id(faixa.getId())
                .inicio(faixa.getInicio())
                .fim(faixa.getFim())
                .valorUnitario(faixa.getValorUnitario())
                .build())
            .collect(Collectors.toList());
    }

    // Exclui uma tabela tarifaria (hard delete com cascade).
    @Transactional

    public void excluir(Long id) {
        TabelaTarifaria tabela = tabelaTarifariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tabela tarifaria com ID " + id + " nao encontrada"));
        tabelaTarifariaRepository.delete(tabela);
    }

    // METODOS DE VALIDACAO
        
    private void validarFaixas(List<FaixaRequest> faixas, String categoriaNome) {
        if (faixas == null || faixas.isEmpty()) {
            throw new BusinessException(
                "A categoria '" + categoriaNome
                + "' deve ter pelo menos uma faixa de consumo");
        }

        // Ordenar por inicio
        List<FaixaRequest> faixasOrdenadas = faixas.stream()
                .sorted(Comparator.comparingInt(FaixaRequest::getInicio))
                .collect(Collectors.toList());

        // Regra 1: Ordem valida (inicio < fim)
        for (FaixaRequest faixa : faixasOrdenadas) {
            if (faixa.getInicio() >= faixa.getFim()) {
                throw new BusinessException(
                    "Categoria '" + categoriaNome + "': O inicio da faixa ("
                    + faixa.getInicio() + ") deve ser menor que o fim ("
                    + faixa.getFim() + ")");
            }
        }

        // Regra 2: Cobertura completa (deve iniciar em 0)
        if (faixasOrdenadas.get(0).getInicio() != 0) {
            throw new BusinessException(
                "Categoria '" + categoriaNome
                + "': A primeira faixa deve iniciar em 0 m3");
        }

        // Regra 3 e 4: Nao sobreposicao e sem lacunas
        for (int i = 1; i < faixasOrdenadas.size(); i++) {
            FaixaRequest anterior = faixasOrdenadas.get(i - 1);
            FaixaRequest atual = faixasOrdenadas.get(i);

            int esperadoInicio = anterior.getFim() + 1;

            if (atual.getInicio() < esperadoInicio) {
                throw new BusinessException(
                    "Categoria '" + categoriaNome
                    + "': Sobreposicao detectada entre faixas ["
                    + anterior.getInicio() + "-" + anterior.getFim()
                    + "] e [" + atual.getInicio() + "-" + atual.getFim() + "]");
            }

            if (atual.getInicio() > esperadoInicio) {
                throw new BusinessException(
                    "Categoria '" + categoriaNome
                    + "': Lacuna detectada entre faixas ["
                    + anterior.getInicio() + "-" + anterior.getFim()
                    + "] e [" + atual.getInicio() + "-" + atual.getFim()
                    + "]. Esperado inicio em " + esperadoInicio);
            }
        }
    }

    // Converte string para CategoriaEnum com validacao.
    
    private CategoriaEnum parseCategoriaEnum(String categoria) {
        try {
            return CategoriaEnum.valueOf(categoria.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                "Categoria invalida: '" + categoria
                + "'. Valores aceitos: COMERCIAL, INDUSTRIAL, PARTICULAR, PUBLICO");
        }
    }

    // METODOS DE CONVERSAO (Entity -> DTO)

    private TabelaTarifariaResponse toResponse(TabelaTarifaria tabela) {
        List<CategoriaResponse> categoriasResp = tabela.getCategorias().stream()
                .map(cat -> CategoriaResponse.builder()
                        .id(cat.getId())
                        .categoria(cat.getCategoria().name())
                        .faixas(cat.getFaixas().stream()
                                .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
                                .map(faixa -> FaixaResponse.builder()
                                        .id(faixa.getId())
                                        .inicio(faixa.getInicio())
                                        .fim(faixa.getFim())
                                        .valorUnitario(faixa.getValorUnitario())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return TabelaTarifariaResponse.builder()
                .id(tabela.getId())
                .nome(tabela.getNome())
                .dataVigencia(tabela.getDataVigencia())
                .ativa(tabela.getAtiva())
                .createdAt(tabela.getCreatedAt())
                .categorias(categoriasResp)
                .build();
    }
}
