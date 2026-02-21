package com.gruporas.watertariffapi.service;

import com.gruporas.watertariffapi.dto.*;
import com.gruporas.watertariffapi.exception.BusinessException;
import com.gruporas.watertariffapi.exception.ResourceNotFoundException;
import com.gruporas.watertariffapi.model.*;
import com.gruporas.watertariffapi.repository.TabelaTarifariaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class TabelaTarifariaServiceTest {

    @Mock

    private TabelaTarifariaRepository tabelaTarifariaRepository;

    @InjectMocks

    private TabelaTarifariaService tabelaTarifariaService;
    private TabelaTarifariaRequest requestValido;
    private TabelaTarifaria tabelaMock;

    // ── Helper: cria uma CategoriaRequest valida (faixas corretas) para a categoria informada ──
    private CategoriaRequest criarCategoriaValida(String categoria) {
        CategoriaRequest catReq = new CategoriaRequest();
        catReq.setCategoria(categoria);
        catReq.setFaixas(List.of(
            new FaixaRequest(0, 10, new BigDecimal("5.00")),
            new FaixaRequest(11, 20, new BigDecimal("8.00")),
            new FaixaRequest(21, 30, new BigDecimal("12.00")),
            new FaixaRequest(31, 99999, new BigDecimal("15.00"))
        ));
        return catReq;
    }

    // ── Helper: cria um request com as 4 categorias obrigatorias (todas validas) ──
    //    Permite substituir a categoria COMERCIAL por uma customizada para testar validacoes de faixas
    private TabelaTarifariaRequest criarRequestCom4Categorias(CategoriaRequest categoriaComercialCustom) {
        TabelaTarifariaRequest request = new TabelaTarifariaRequest();
        request.setNome("Tabela Invalida");
        request.setDataVigencia(LocalDate.of(2025, 1, 1));
        request.setCategorias(List.of(
            categoriaComercialCustom,
            criarCategoriaValida("INDUSTRIAL"),
            criarCategoriaValida("PARTICULAR"),
            criarCategoriaValida("PUBLICO")
        ));
        return request;
    }

    @BeforeEach

    void setUp() {

        // Montar request valido com as 4 categorias obrigatorias
        requestValido = new TabelaTarifariaRequest();
        requestValido.setNome("Tabela Teste 2025");
        requestValido.setDataVigencia(LocalDate.of(2025, 1, 1));

        requestValido.setCategorias(List.of(
            criarCategoriaValida("COMERCIAL"),
            criarCategoriaValida("INDUSTRIAL"),
            criarCategoriaValida("PARTICULAR"),
            criarCategoriaValida("PUBLICO")
        ));

        // Montar entidade mock para retorno do repository
        tabelaMock = TabelaTarifaria.builder()
                .id(1L)
                .nome("Tabela Teste 2025")
                .dataVigencia(LocalDate.of(2025, 1, 1))
                .ativa(true)
                .createdAt(LocalDateTime.now())
                .build();

        CategoriaTarifa catMockComercial = criarCategoriaTarifaMock(1L, CategoriaEnum.COMERCIAL, tabelaMock, 1L);
        CategoriaTarifa catMockIndustrial = criarCategoriaTarifaMock(2L, CategoriaEnum.INDUSTRIAL, tabelaMock, 5L);
        CategoriaTarifa catMockParticular = criarCategoriaTarifaMock(3L, CategoriaEnum.PARTICULAR, tabelaMock, 9L);
        CategoriaTarifa catMockPublico = criarCategoriaTarifaMock(4L, CategoriaEnum.PUBLICO, tabelaMock, 13L);

        tabelaMock.setCategorias(new ArrayList<>(List.of(
            catMockComercial, catMockIndustrial, catMockParticular, catMockPublico
        )));
    }

    // ── Helper: cria uma CategoriaTarifa mock com 4 faixas padrao ──
    private CategoriaTarifa criarCategoriaTarifaMock(Long catId, CategoriaEnum categoria,
                                                     TabelaTarifaria tabela, Long startFaixaId) {
        CategoriaTarifa cat = CategoriaTarifa.builder()
                .id(catId)
                .categoria(categoria)
                .tabelaTarifaria(tabela)
                .build();

        cat.setFaixas(List.of(
            FaixaConsumo.builder().id(startFaixaId).inicio(0).fim(10)
                .valorUnitario(new BigDecimal("5.00")).categoriaTarifa(cat).build(),
            FaixaConsumo.builder().id(startFaixaId + 1).inicio(11).fim(20)
                .valorUnitario(new BigDecimal("8.00")).categoriaTarifa(cat).build(),
            FaixaConsumo.builder().id(startFaixaId + 2).inicio(21).fim(30)
                .valorUnitario(new BigDecimal("12.00")).categoriaTarifa(cat).build(),
            FaixaConsumo.builder().id(startFaixaId + 3).inicio(31).fim(99999)
                .valorUnitario(new BigDecimal("15.00")).categoriaTarifa(cat).build()
        ));

        return cat;
    }

    // TESTES DE CRIACAO
    @Nested
    @DisplayName("Testes do metodo criar()")

    class CriarTests {

        @Test
        @DisplayName("Deve criar tabela tarifaria com sucesso quando dados validos")
        void deveCriarTabelaComSucesso() {

            // Arrange
            when(tabelaTarifariaRepository.save(any(TabelaTarifaria.class)))
                    .thenReturn(tabelaMock);

            // Act
            TabelaTarifariaResponse response = tabelaTarifariaService.criar(requestValido);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getNome()).isEqualTo("Tabela Teste 2025");
            assertThat(response.getAtiva()).isTrue();
            assertThat(response.getCategorias()).hasSize(4);
            assertThat(response.getCategorias().get(0).getCategoria()).isEqualTo("COMERCIAL");
            assertThat(response.getCategorias().get(0).getFaixas()).hasSize(4);

            // Verificar que o repository.save() foi chamado exatamente uma vez
            verify(tabelaTarifariaRepository, times(1)).save(any(TabelaTarifaria.class));
        }

        @Test
        @DisplayName("Deve persistir entidade com dados corretos via ArgumentCaptor")

        void devePersistirEntidadeCorretamente() {

            // Arrange
            ArgumentCaptor<TabelaTarifaria> captor = ArgumentCaptor.forClass(TabelaTarifaria.class);
            when(tabelaTarifariaRepository.save(captor.capture()))
                    .thenReturn(tabelaMock);

            // Act
            tabelaTarifariaService.criar(requestValido);

            // Assert - verificar o que foi passado ao save()
            TabelaTarifaria entidadeSalva = captor.getValue();
            assertThat(entidadeSalva.getNome()).isEqualTo("Tabela Teste 2025");
            assertThat(entidadeSalva.getDataVigencia()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(entidadeSalva.getAtiva()).isTrue();
            assertThat(entidadeSalva.getCategorias()).hasSize(4);
            assertThat(entidadeSalva.getCategorias().get(0).getFaixas()).hasSize(4);
        }

        @Test
        @DisplayName("Deve criar tabela com multiplas categorias")

        void deveCriarTabelaComMultiplasCategorias() {

            // Arrange - request com as 4 categorias obrigatorias (cada uma com faixas proprias)
            CategoriaRequest catComercial = new CategoriaRequest();
            catComercial.setCategoria("COMERCIAL");
            catComercial.setFaixas(List.of(
                new FaixaRequest(0, 10, new BigDecimal("5.00")),
                new FaixaRequest(11, 99999, new BigDecimal("8.00"))
            ));

            CategoriaRequest catIndustrial = new CategoriaRequest();
            catIndustrial.setCategoria("INDUSTRIAL");
            catIndustrial.setFaixas(List.of(
                new FaixaRequest(0, 10, new BigDecimal("4.00")),
                new FaixaRequest(11, 99999, new BigDecimal("6.50"))
            ));

            TabelaTarifariaRequest multiRequest = new TabelaTarifariaRequest();
            multiRequest.setNome("Tabela Multi");
            multiRequest.setDataVigencia(LocalDate.of(2025, 6, 1));
            multiRequest.setCategorias(List.of(
                catComercial,
                catIndustrial,
                criarCategoriaValida("PARTICULAR"),
                criarCategoriaValida("PUBLICO")
            ));

            when(tabelaTarifariaRepository.save(any(TabelaTarifaria.class)))
                    .thenReturn(tabelaMock);

            // Act
            TabelaTarifariaResponse response = tabelaTarifariaService.criar(multiRequest);

            // Assert
            assertThat(response.getCategorias()).hasSize(4);
        }
    }

    // TESTES DE VALIDACAO DE FAIXAS
    @Nested
    @DisplayName("Testes de validacao de faixas de consumo")

    class ValidacaoFaixasTests {

        @Test
        @DisplayName("Deve rejeitar faixas com sobreposicao")

        void deveRejeitarFaixasComSobreposicao() {
            // Categoria COMERCIAL com faixas invalidas (sobreposicao)
            CategoriaRequest catComercialInvalida = new CategoriaRequest();
            catComercialInvalida.setCategoria("COMERCIAL");
            catComercialInvalida.setFaixas(List.of(
                new FaixaRequest(0, 10, new BigDecimal("5.00")),
                new FaixaRequest(8, 20, new BigDecimal("8.00"))  // Sobreposicao: 8 < 11
            ));

            // Envia as 4 categorias obrigatorias, com COMERCIAL contendo faixas invalidas
            TabelaTarifariaRequest request = criarRequestCom4Categorias(catComercialInvalida);

            assertThatThrownBy(() -> tabelaTarifariaService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sobreposicao detectada");
        }

        @Test
        @DisplayName("Deve rejeitar faixas com lacuna entre intervalos")

        void deveRejeitarFaixasComLacuna() {
            // Categoria COMERCIAL com faixas invalidas (lacuna)
            CategoriaRequest catComercialInvalida = new CategoriaRequest();
            catComercialInvalida.setCategoria("COMERCIAL");
            catComercialInvalida.setFaixas(List.of(
                new FaixaRequest(0, 10, new BigDecimal("5.00")),
                new FaixaRequest(15, 20, new BigDecimal("8.00"))  // Lacuna: 11-14 sem cobertura
            ));

            // Envia as 4 categorias obrigatorias, com COMERCIAL contendo faixas invalidas
            TabelaTarifariaRequest request = criarRequestCom4Categorias(catComercialInvalida);

            assertThatThrownBy(() -> tabelaTarifariaService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Lacuna detectada");
        }

        @Test
        @DisplayName("Deve rejeitar faixas que nao iniciam em 0")

        void deveRejeitarFaixasQueNaoIniciamEmZero() {
            // Categoria COMERCIAL com faixas invalidas (nao inicia em 0)
            CategoriaRequest catComercialInvalida = new CategoriaRequest();
            catComercialInvalida.setCategoria("COMERCIAL");
            catComercialInvalida.setFaixas(List.of(
                new FaixaRequest(5, 10, new BigDecimal("5.00")),  // Nao inicia em 0
                new FaixaRequest(11, 20, new BigDecimal("8.00"))
            ));

            // Envia as 4 categorias obrigatorias, com COMERCIAL contendo faixas invalidas
            TabelaTarifariaRequest request = criarRequestCom4Categorias(catComercialInvalida);

            assertThatThrownBy(() -> tabelaTarifariaService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("primeira faixa deve iniciar em 0");
        }

        @Test
        @DisplayName("Deve rejeitar faixa com inicio maior ou igual ao fim")

        void deveRejeitarFaixaComInicioMaiorOuIgualAoFim() {
            // Categoria COMERCIAL com faixa invalida (inicio == fim)
            CategoriaRequest catComercialInvalida = new CategoriaRequest();
            catComercialInvalida.setCategoria("COMERCIAL");
            catComercialInvalida.setFaixas(List.of(
                new FaixaRequest(0, 0, new BigDecimal("5.00"))  // inicio == fim
            ));

            // Envia as 4 categorias obrigatorias, com COMERCIAL contendo faixa invalida
            TabelaTarifariaRequest request = criarRequestCom4Categorias(catComercialInvalida);

            assertThatThrownBy(() -> tabelaTarifariaService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inicio da faixa")
                    .hasMessageContaining("deve ser menor que o fim");
        }

        @Test
        @DisplayName("Deve rejeitar categoria invalida")

        void deveRejeitarCategoriaInvalida() {
            // Envia 4 categorias, mas uma delas e invalida (RESIDENCIAL no lugar de COMERCIAL)
            CategoriaRequest catResidencial = new CategoriaRequest();
            catResidencial.setCategoria("RESIDENCIAL");  // Categoria inexistente
            catResidencial.setFaixas(List.of(
                new FaixaRequest(0, 10, new BigDecimal("5.00")),
                new FaixaRequest(11, 20, new BigDecimal("8.00")),
                new FaixaRequest(21, 30, new BigDecimal("12.00")),
                new FaixaRequest(31, 99999, new BigDecimal("15.00"))
            ));

            TabelaTarifariaRequest request = new TabelaTarifariaRequest();
            request.setNome("Tabela Invalida");
            request.setDataVigencia(LocalDate.of(2025, 1, 1));
            request.setCategorias(List.of(
                catResidencial,
                criarCategoriaValida("INDUSTRIAL"),
                criarCategoriaValida("PARTICULAR"),
                criarCategoriaValida("PUBLICO")
            ));

            assertThatThrownBy(() -> tabelaTarifariaService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("invalida(s)")
                    .hasMessageContaining("RESIDENCIAL");
        }

        @Test
        @DisplayName("Deve aceitar faixas desordenadas e validar corretamente")

        void deveAceitarFaixasDesordenadas() {

            // Faixas enviadas fora de ordem, mas validas quando ordenadas
            CategoriaRequest catReqDesordenada = new CategoriaRequest();
            catReqDesordenada.setCategoria("COMERCIAL");
            catReqDesordenada.setFaixas(List.of(
                new FaixaRequest(21, 30, new BigDecimal("12.00")),
                new FaixaRequest(0, 10, new BigDecimal("5.00")),
                new FaixaRequest(31, 99999, new BigDecimal("15.00")),
                new FaixaRequest(11, 20, new BigDecimal("8.00"))
            ));

            TabelaTarifariaRequest request = new TabelaTarifariaRequest();
            request.setNome("Tabela Desordenada");
            request.setDataVigencia(LocalDate.of(2025, 1, 1));
            request.setCategorias(List.of(
                catReqDesordenada,
                criarCategoriaValida("INDUSTRIAL"),
                criarCategoriaValida("PARTICULAR"),
                criarCategoriaValida("PUBLICO")
            ));

            when(tabelaTarifariaRepository.save(any(TabelaTarifaria.class)))
                    .thenReturn(tabelaMock);

            // Act - nao deve lancar excecao
            TabelaTarifariaResponse response = tabelaTarifariaService.criar(request);

            // Assert
            assertThat(response).isNotNull();
            verify(tabelaTarifariaRepository).save(any(TabelaTarifaria.class));
        }
    }

    // TESTES DE LISTAGEM
    @Nested
    @DisplayName("Testes do metodo listarTodas()")

    class ListarTests {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha tabelas")

        void deveRetornarListaVazia() {
            when(tabelaTarifariaRepository.findAll()).thenReturn(List.of());

            List<TabelaTarifariaResponse> resultado = tabelaTarifariaService.listarTodas();

            assertThat(resultado).isEmpty();
            verify(tabelaTarifariaRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar todas as tabelas cadastradas")

        void deveRetornarTodasAsTabelas() {
            TabelaTarifaria tabela2 = TabelaTarifaria.builder()
                    .id(2L).nome("Tabela 2025-02")
                    .dataVigencia(LocalDate.of(2025, 2, 1))
                    .ativa(true).createdAt(LocalDateTime.now())
                    .build();
            tabela2.setCategorias(new ArrayList<>());

            when(tabelaTarifariaRepository.findAll())
                    .thenReturn(List.of(tabelaMock, tabela2));

            List<TabelaTarifariaResponse> resultado = tabelaTarifariaService.listarTodas();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getId()).isEqualTo(1L);
            assertThat(resultado.get(1).getId()).isEqualTo(2L);
        }
    }

    // TESTES DE BUSCA POR ID
    @Nested
    @DisplayName("Testes do metodo buscarPorId()")

    class BuscarPorIdTests {

        @Test
        @DisplayName("Deve retornar tabela quando ID existe")

        void deveRetornarTabelaQuandoIdExiste() {
            when(tabelaTarifariaRepository.findById(1L))
                    .thenReturn(Optional.of(tabelaMock));

            TabelaTarifariaResponse response = tabelaTarifariaService.buscarPorId(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getNome()).isEqualTo("Tabela Teste 2025");
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando ID nao existe")

        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(tabelaTarifariaRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tabelaTarifariaService.buscarPorId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999")
                    .hasMessageContaining("nao encontrada");
        }
    }

    // TESTES DE EXCLUSAO
    @Nested
    @DisplayName("Testes do metodo excluir()")

    class ExcluirTests {

        @Test
        @DisplayName("Deve excluir tabela quando ID existe")

        void deveExcluirTabelaQuandoIdExiste() {
            when(tabelaTarifariaRepository.findById(1L))
                    .thenReturn(Optional.of(tabelaMock));
            doNothing().when(tabelaTarifariaRepository).delete(tabelaMock);

            // Act - nao deve lancar excecao
            tabelaTarifariaService.excluir(1L);

            // Assert
            verify(tabelaTarifariaRepository).findById(1L);
            verify(tabelaTarifariaRepository).delete(tabelaMock);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException ao excluir ID inexistente")

        void deveLancarExcecaoAoExcluirIdInexistente() {
            when(tabelaTarifariaRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tabelaTarifariaService.excluir(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999")
                    .hasMessageContaining("nao encontrada");

            verify(tabelaTarifariaRepository, never()).delete(any());
        }
    }

    // TESTES DE CONVERSAO (toResponse)
    @Nested
    @DisplayName("Testes de conversao Entity -> Response")

    class ConversaoTests {

        @Test
        @DisplayName("Deve converter corretamente faixas ordenadas por inicio")

        void deveConverterFaixasOrdenadas() {
            when(tabelaTarifariaRepository.findById(1L))
                    .thenReturn(Optional.of(tabelaMock));

            TabelaTarifariaResponse response = tabelaTarifariaService.buscarPorId(1L);

            List<FaixaResponse> faixas = response.getCategorias().get(0).getFaixas();
            assertThat(faixas).hasSize(4);

            // Verificar ordenacao por inicio
            for (int i = 1; i < faixas.size(); i++) {
                assertThat(faixas.get(i).getInicio())
                        .isGreaterThan(faixas.get(i - 1).getInicio());
            }
        }

        @Test
        @DisplayName("Deve mapear todos os campos da Response corretamente")

        void deveMapearTodosCampos() {
            when(tabelaTarifariaRepository.findById(1L))
                    .thenReturn(Optional.of(tabelaMock));

            TabelaTarifariaResponse response = tabelaTarifariaService.buscarPorId(1L);

            // Campos da tabela
            assertThat(response.getId()).isNotNull();
            assertThat(response.getNome()).isNotBlank();
            assertThat(response.getDataVigencia()).isNotNull();
            assertThat(response.getAtiva()).isNotNull();
            assertThat(response.getCreatedAt()).isNotNull();

            // Campos da categoria
            CategoriaResponse cat = response.getCategorias().get(0);
            assertThat(cat.getId()).isNotNull();
            assertThat(cat.getCategoria()).isEqualTo("COMERCIAL");

            // Campos da faixa
            FaixaResponse faixa = cat.getFaixas().get(0);
            assertThat(faixa.getId()).isNotNull();
            assertThat(faixa.getInicio()).isEqualTo(0);
            assertThat(faixa.getFim()).isEqualTo(10);
            assertThat(faixa.getValorUnitario()).isEqualByComparingTo(new BigDecimal("5.00"));
        }
    }
}
