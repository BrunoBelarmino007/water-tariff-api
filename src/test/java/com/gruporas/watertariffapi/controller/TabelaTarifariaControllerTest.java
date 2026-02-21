package com.gruporas.watertariffapi.controller;

import com.gruporas.watertariffapi.dto.*;
import com.gruporas.watertariffapi.exception.BusinessException;
import com.gruporas.watertariffapi.exception.ResourceNotFoundException;
import com.gruporas.watertariffapi.service.TabelaTarifariaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;              // corrigido
import org.springframework.test.context.bean.override.mockito.MockitoBean;         // corrigido

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TabelaTarifariaController.class)
class TabelaTarifariaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean                                                               
    private TabelaTarifariaService tabelaTarifariaService;
    private TabelaTarifariaResponse responseMock;
    private String requestJsonValido;

    @BeforeEach

    void setUp() {

        // Response mock padrao
        responseMock = TabelaTarifariaResponse.builder()
                .id(1L)
                .nome("Tabela Teste 2025")
                .dataVigencia(LocalDate.of(2025, 1, 1))
                .ativa(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
                .categorias(List.of(
                    CategoriaResponse.builder()
                        .id(1L)
                        .categoria("COMERCIAL")
                        .faixas(List.of(
                            FaixaResponse.builder()
                                .id(1L).inicio(0).fim(10)
                                .valorUnitario(new BigDecimal("5.00")).build(),
                            FaixaResponse.builder()
                                .id(2L).inicio(11).fim(20)
                                .valorUnitario(new BigDecimal("8.00")).build(),
                            FaixaResponse.builder()
                                .id(3L).inicio(21).fim(30)
                                .valorUnitario(new BigDecimal("12.00")).build(),
                            FaixaResponse.builder()
                                .id(4L).inicio(31).fim(99999)
                                .valorUnitario(new BigDecimal("15.00")).build()
                        ))
                        .build()
                ))
                .build();

        // Request JSON valido
        requestJsonValido = """
            {
                "nome": "Tabela Teste 2025",
                "dataVigencia": "2025-01-01",
                "categorias": [
                    {
                        "categoria": "COMERCIAL",
                        "faixas": [
                            {"inicio": 0, "fim": 10, "valorUnitario": 5.00},
                            {"inicio": 11, "fim": 20, "valorUnitario": 8.00},
                            {"inicio": 21, "fim": 30, "valorUnitario": 12.00},
                            {"inicio": 31, "fim": 99999, "valorUnitario": 15.00}
                        ]
                    }
                ]
            }
            """;
    }

    // TESTES POST /api/tabelas-tarifarias
    @Nested
    @DisplayName("POST /api/tabelas-tarifarias")

    class CriarEndpointTests {

        @Test
        @DisplayName("Deve retornar 201 Created ao criar tabela valida")

        void deveRetornar201AoCriarTabelaValida() throws Exception {
            when(tabelaTarifariaService.criar(any(TabelaTarifariaRequest.class)))
                    .thenReturn(responseMock);

            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJsonValido))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Tabela Teste 2025"))
                    .andExpect(jsonPath("$.dataVigencia").value("2025-01-01"))
                    .andExpect(jsonPath("$.ativa").value(true))
                    .andExpect(jsonPath("$.categorias").isArray())
                    .andExpect(jsonPath("$.categorias.length()").value(1))
                    .andExpect(jsonPath("$.categorias[0].categoria").value("COMERCIAL"))
                    .andExpect(jsonPath("$.categorias[0].faixas.length()").value(4));
        }

        @Test
        @DisplayName("Deve retornar 400 quando nome esta ausente")

        void deveRetornar400QuandoNomeAusente() throws Exception {
            String requestSemNome = """
                {
                    "dataVigencia": "2025-01-01",
                    "categorias": [
                        {
                            "categoria": "COMERCIAL",
                            "faixas": [
                                {"inicio": 0, "fim": 10, "valorUnitario": 5.00}
                            ]
                        }
                    ]
                }
                """;

            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestSemNome))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando dataVigencia esta ausente")

        void deveRetornar400QuandoDataVigenciaAusente() throws Exception {
            String requestSemData = """
                {
                    "nome": "Tabela Teste",
                    "categorias": [
                        {
                            "categoria": "COMERCIAL",
                            "faixas": [
                                {"inicio": 0, "fim": 10, "valorUnitario": 5.00}
                            ]
                        }
                    ]
                }
                """;

            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestSemData))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando lista de categorias esta vazia")

        void deveRetornar400QuandoCategoriasVazia() throws Exception {
            String requestSemCategorias = """
                {
                    "nome": "Tabela Teste",
                    "dataVigencia": "2025-01-01",
                    "categorias": []
                }
                """;

            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestSemCategorias))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando faixas tem sobreposicao (regra de negocio)")

        void deveRetornar400QuandoFaixasComSobreposicao() throws Exception {
            when(tabelaTarifariaService.criar(any(TabelaTarifariaRequest.class)))
                    .thenThrow(new BusinessException(
                        "Categoria 'COMERCIAL': Sobreposicao detectada entre faixas [0-10] e [8-20]"));

            String requestComSobreposicao = """
                {
                    "nome": "Tabela Invalida",
                    "dataVigencia": "2025-01-01",
                    "categorias": [
                        {
                            "categoria": "COMERCIAL",
                            "faixas": [
                                {"inicio": 0, "fim": 10, "valorUnitario": 5.00},
                                {"inicio": 8, "fim": 20, "valorUnitario": 8.00}
                            ]
                        }
                    ]
                }
                """;

            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestComSobreposicao))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(
                        containsString("Sobreposicao detectada")));
        }

        @Test
        @DisplayName("Deve retornar 400 com body vazio")

        void deveRetornar400ComBodyVazio() throws Exception {
            mockMvc.perform(post("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // TESTES GET /api/tabelas-tarifarias
    @Nested
    @DisplayName("GET /api/tabelas-tarifarias")

    class ListarEndpointTests {

        @Test
        @DisplayName("Deve retornar 200 com lista de tabelas")

        void deveRetornar200ComListaDeTabelas() throws Exception {
            when(tabelaTarifariaService.listarTodas())
                    .thenReturn(List.of(responseMock));

            mockMvc.perform(get("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].nome").value("Tabela Teste 2025"))
                    .andExpect(jsonPath("$[0].categorias[0].categoria").value("COMERCIAL"));
        }

        @Test
        @DisplayName("Deve retornar 200 com lista vazia quando nao ha tabelas")

        void deveRetornar200ComListaVazia() throws Exception {
            when(tabelaTarifariaService.listarTodas())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/tabelas-tarifarias")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // TESTES GET /api/tabelas-tarifarias/{id}
    @Nested
    @DisplayName("GET /api/tabelas-tarifarias/{id}")

    class BuscarPorIdEndpointTests {

        @Test
        @DisplayName("Deve retornar 200 quando tabela existe")

        void deveRetornar200QuandoTabelaExiste() throws Exception {
            when(tabelaTarifariaService.buscarPorId(1L))
                    .thenReturn(responseMock);

            mockMvc.perform(get("/api/tabelas-tarifarias/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Tabela Teste 2025"))
                    .andExpect(jsonPath("$.categorias").isArray());
        }

        @Test
        @DisplayName("Deve retornar 404 quando tabela nao existe")

        void deveRetornar404QuandoTabelaNaoExiste() throws Exception {
            when(tabelaTarifariaService.buscarPorId(999L))
                    .thenThrow(new ResourceNotFoundException(
                        "Tabela tarifaria com ID 999 nao encontrada"));

            mockMvc.perform(get("/api/tabelas-tarifarias/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensagem").value(
                        containsString("999")));
        }
    }

    // TESTES DELETE /api/tabelas-tarifarias/{id}
    @Nested
    @DisplayName("DELETE /api/tabelas-tarifarias/{id}")

    class ExcluirEndpointTests {

        @Test
        @DisplayName("Deve retornar 204 No Content ao excluir tabela existente")

        void deveRetornar204AoExcluirTabelaExistente() throws Exception {
            doNothing().when(tabelaTarifariaService).excluir(1L);

            mockMvc.perform(delete("/api/tabelas-tarifarias/1"))
                    .andExpect(status().isNoContent());

            verify(tabelaTarifariaService).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar excluir tabela inexistente")
        
        void deveRetornar404AoExcluirTabelaInexistente() throws Exception {
            doThrow(new ResourceNotFoundException(
                "Tabela tarifaria com ID 999 nao encontrada"))
                .when(tabelaTarifariaService).excluir(999L);

            mockMvc.perform(delete("/api/tabelas-tarifarias/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensagem").value(
                        containsString("999")));
        }
    }
}
