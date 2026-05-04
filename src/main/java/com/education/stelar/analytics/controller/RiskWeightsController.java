package com.education.stelar.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.analytics.dto.request.UpdateRiskWeightsRequest;
import com.education.stelar.analytics.dto.response.RiskWeightsResponse;
import com.education.stelar.analytics.service.RiskWeightsService;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
@Tag(name = "Risk Weights", description = "Configuración de pesos del algoritmo de riesgo")
public class RiskWeightsController {

    private final RiskWeightsService riskWeightsService;

    @GetMapping("/weights")
    @Operation(summary = "Obtener pesos actuales del algoritmo de riesgo")
    public ResponseEntity<RiskWeightsResponse> getWeights() {
        return ResponseEntity.ok(riskWeightsService.getWeights());
    }

    @PutMapping("/weights")
    @Operation(summary = "Actualizar pesos del algoritmo de riesgo (deben sumar 1.0)")
    public ResponseEntity<RiskWeightsResponse> updateWeights(@Valid @RequestBody UpdateRiskWeightsRequest request) {
        return ResponseEntity.ok(riskWeightsService.updateWeights(request));
    }
}
