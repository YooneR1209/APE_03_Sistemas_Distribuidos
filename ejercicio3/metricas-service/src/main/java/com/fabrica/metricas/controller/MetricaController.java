package com.fabrica.metricas.controller;

import com.fabrica.metricas.service.ContadoresMetrica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/metricas")
@CrossOrigin(origins = "*")
public class MetricaController {

    private final ContadoresMetrica contadores;

    public MetricaController(ContadoresMetrica contadores) { this.contadores = contadores; }

    @PostMapping("/evento")
    public ResponseEntity<Map<String, Object>> evento(@RequestBody Map<String, Object> cuerpo) {
        contadores.registrar(String.valueOf(cuerpo.getOrDefault("tipo", "DESCONOCIDO")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() { return contadores.resumen(); }

    @PostMapping("/reiniciar")
    public ResponseEntity<Map<String, Object>> reiniciar() {
        contadores.reiniciar();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
