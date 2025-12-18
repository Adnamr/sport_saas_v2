package com.sportsaas.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller de santé pour vérifier que l'API fonctionne.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Vérification de l'état de l'API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Vérifier l'état de l'API")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "sport-saas-api"
        ));
    }
}
