package com.gruporas.watertariffapi.service;

import com.gruporas.watertariffapi.dto.CalculoRequest;
import com.gruporas.watertariffapi.dto.CalculoResponse;
import com.gruporas.watertariffapi.exception.BusinessException;
import com.gruporas.watertariffapi.exception.ResourceNotFoundException;
import com.gruporas.watertariffapi.model.*;
import com.gruporas.watertariffapi.repository.CategoriaTarifaRepository;
import com.gruporas.watertariffapi.repository.TabelaTarifariaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class CalculoServiceTest {

    @Mock

    private TabelaTarifariaRepository tabelaTarifariaRepository;

    @Mock

    private CategoriaTarifaRepository categoriaTarifaRepository;

    @InjectMocks

    private CalculoService calculoService;

    private TabelaTarifaria tabelaMock;
    private CategoriaTarifa categoriaMock;

    @BeforeEach

    void setUp() {

        // Montar tabela mock com faixas industriais
        tabelaMock = TabelaTarifaria.builder()
                .id(1L)
                .nome("Tabela Teste")
                .dataVigencia(LocalDate.of(2025, 1, 1))
                .ativa(true)
                .build();

        categoriaMock = CategoriaTarifa.builder()
                .id(1L)
                .categoria(CategoriaEnum.INDUSTRIAL)
                .tabelaTarifaria(tabelaMock)
                .build();

        // Adicionar faixas
        categoriaMock.setFaixas(List.of(
            FaixaConsumo.builder().id(1L).inicio(0).fim(10)
                .valorUnitario(new BigDecimal("1.00"))
                .categoriaTarifa(categoriaMock).build(),
            FaixaConsumo.builder().id(2L).inicio(11).fim(20)
                .valorUnitario(new BigDecimal("2.00"))
                .categoriaTarifa(categoriaMock).build(),
            FaixaConsumo.builder().id(3L).inicio(21).fim(30)
                .valorUnitario(new BigDecimal("3.00"))
                .categoriaTarifa(categoriaMock).build(),
            FaixaConsumo.builder().id(4L).inicio(31).fim(99999)
                .valorUnitario(new BigDecimal("4.00"))
                .categoriaTarifa(categoriaMock).build()
        ));
    }

    
    @Test
    @DisplayName("Deve calcular corretamente consumo de 18m3 INDUSTRIAL")

    void deveCalcularConsumo18m3() {
    
        // Arrange
        when(tabelaTarifariaRepository.findLatestAtiva())
                .thenReturn(Optional.of(tabelaMock));
        when(categoriaTarifaRepository
                .findByTabelaTarifariaIdAndCategoria(1L, CategoriaEnum.INDUSTRIAL))
                .thenReturn(Optional.of(categoriaMock));

        CalculoRequest request = new CalculoRequest("INDUSTRIAL", 18);

        // Act
        CalculoResponse response = calculoService.calcular(request);

        // Assert
        assertThat(response.getCategoria()).isEqualTo("INDUSTRIAL");
        assertThat(response.getConsumoTotal()).isEqualTo(18);
        assertThat(response.getValorTotal()).isEqualByComparingTo(new BigDecimal("26.00"));
        assertThat(response.getDetalhamento()).hasSize(2);

        // Faixa 1: 10 x R$1.00 = R$10.00
        assertThat(response.getDetalhamento().get(0).getM3Cobrados()).isEqualTo(10);
        assertThat(response.getDetalhamento().get(0).getSubtotal())
                .isEqualByComparingTo(new BigDecimal("10.00"));

        // Faixa 2: 8 x R$2.00 = R$16.00
        assertThat(response.getDetalhamento().get(1).getM3Cobrados()).isEqualTo(8);
        assertThat(response.getDetalhamento().get(1).getSubtotal())
                .isEqualByComparingTo(new BigDecimal("16.00"));
    }

    @Test
    @DisplayName("Deve lancar excecao para categoria invalida")
    
    void deveLancarExcecaoParaCategoriaInvalida() {
        CalculoRequest request = new CalculoRequest("INVALIDA", 10);

        assertThatThrownBy(() -> calculoService.calcular(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Categoria invalida");
    }

    @Test
    @DisplayName("Deve lancar excecao quando nenhuma tabela ativa")
    
    void deveLancarExcecaoSemTabelaAtiva() {
        when(tabelaTarifariaRepository.findLatestAtiva())
                .thenReturn(Optional.empty());

        CalculoRequest request = new CalculoRequest("INDUSTRIAL", 10);

        assertThatThrownBy(() -> calculoService.calcular(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhuma tabela tarifaria ativa");
    }

    @Test
    @DisplayName("Deve calcular consumo zero corretamente")
    
    void deveCalcularConsumoZero() {
        when(tabelaTarifariaRepository.findLatestAtiva())
                .thenReturn(Optional.of(tabelaMock));
        when(categoriaTarifaRepository
                .findByTabelaTarifariaIdAndCategoria(1L, CategoriaEnum.INDUSTRIAL))
                .thenReturn(Optional.of(categoriaMock));

        CalculoRequest request = new CalculoRequest("INDUSTRIAL", 0);
        CalculoResponse response = calculoService.calcular(request);

        assertThat(response.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getDetalhamento()).isEmpty();

    }

}
