package com.gruporas.watertariffapi.controller;

import com.gruporas.watertariffapi.dto.CalculoRequest;
import com.gruporas.watertariffapi.dto.CalculoResponse;
import com.gruporas.watertariffapi.dto.DetalhamentoFaixaResponse;
import com.gruporas.watertariffapi.service.CalculoService;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;          
import org.springframework.test.context.bean.override.mockito.MockitoBean;   
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculoController.class)
class CalculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean                     
    private CalculoService calculoService;

    @Test
    @DisplayName("POST /api/calculos - deve retornar 200 com calculo valido")
    
    void deveRetornar200ComCalculoValido() throws Exception {

        // Arrange
        CalculoResponse mockResponse = CalculoResponse.builder()
                .categoria("INDUSTRIAL")
                .consumoTotal(18)
                .valorTotal(new BigDecimal("26.00"))
                .detalhamento(List.of(
                    DetalhamentoFaixaResponse.builder()
                        .faixa(DetalhamentoFaixaResponse.FaixaInfo.builder()
                            .inicio(0).fim(10).build())
                        .m3Cobrados(10)
                        .valorUnitario(new BigDecimal("1.00"))
                        .subtotal(new BigDecimal("10.00"))
                        .build(),
                    DetalhamentoFaixaResponse.builder()
                        .faixa(DetalhamentoFaixaResponse.FaixaInfo.builder()
                            .inicio(11).fim(20).build())
                        .m3Cobrados(8)
                        .valorUnitario(new BigDecimal("2.00"))
                        .subtotal(new BigDecimal("16.00"))
                        .build()
                ))
                .build();

        when(calculoService.calcular(any(CalculoRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/calculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoria\": \"INDUSTRIAL\", \"consumo\": 18}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("INDUSTRIAL"))
                .andExpect(jsonPath("$.consumoTotal").value(18))
                .andExpect(jsonPath("$.valorTotal").value(26.00))
                .andExpect(jsonPath("$.detalhamento").isArray())
                .andExpect(jsonPath("$.detalhamento.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/calculos - deve retornar 400 sem campos obrigatorios")

    void deveRetornar400SemCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/api/calculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
