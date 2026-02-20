package com.gruporas.watertariffapi.controller;

import com.gruporas.watertariffapi.dto.FaixaResponse;
import com.gruporas.watertariffapi.dto.TabelaTarifariaRequest;
import com.gruporas.watertariffapi.dto.TabelaTarifariaResponse;
import com.gruporas.watertariffapi.service.TabelaTarifariaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tabelas-tarifarias")
@RequiredArgsConstructor

public class TabelaTarifariaController {

    private final TabelaTarifariaService tabelaTarifariaService;

    // Cria uma nova tabela tarifaria completa.
    @PostMapping

    public ResponseEntity<TabelaTarifariaResponse> criar(
            @Valid @RequestBody TabelaTarifariaRequest request) {
        TabelaTarifariaResponse response = tabelaTarifariaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Retorna todas as tabelas tarifarias cadastradas.
    @GetMapping

    public ResponseEntity<List<TabelaTarifariaResponse>> listarTodas() {
        List<TabelaTarifariaResponse> response = tabelaTarifariaService.listarTodas();
        return ResponseEntity.ok(response);
    }

    // Retorna uma tabela tarifaria especifica por ID.
    @GetMapping("/{id}")

    public ResponseEntity<TabelaTarifariaResponse> buscarPorId(@PathVariable Long id) {
        TabelaTarifariaResponse response = tabelaTarifariaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    // Remove uma tabela tarifaria do sistema.
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tabelaTarifariaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Retorna as faixas de uma categoria específica dentro de uma tabela tarifária.
    @GetMapping("/{tabelaId}/categorias/{categoria}/faixas")

    public ResponseEntity<List<FaixaResponse>> listarFaixasPorCategoria(
            @PathVariable Long tabelaId,
            @PathVariable String categoria) {

        List<FaixaResponse> response = tabelaTarifariaService
            .listarFaixasPorCategoria(tabelaId, categoria);

        return ResponseEntity.ok(response);
        
    }
}
