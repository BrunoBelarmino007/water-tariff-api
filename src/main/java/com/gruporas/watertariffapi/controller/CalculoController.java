package com.gruporas.watertariffapi.controller;

import com.gruporas.watertariffapi.dto.CalculoRequest;
import com.gruporas.watertariffapi.dto.CalculoResponse;
import com.gruporas.watertariffapi.service.CalculoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor

public class CalculoController {

    private final CalculoService calculoService;

    // Calcula o valor a pagar com base na categoria e consumo informados.
    @PostMapping

    public ResponseEntity<CalculoResponse> calcular(
            @Valid @RequestBody CalculoRequest request) {
        CalculoResponse response = calculoService.calcular(request);
        return ResponseEntity.ok(response);
    }
}
