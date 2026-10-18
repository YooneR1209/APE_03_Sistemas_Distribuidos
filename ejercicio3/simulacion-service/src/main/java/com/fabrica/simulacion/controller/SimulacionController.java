package com.fabrica.simulacion.controller;

import com.fabrica.simulacion.dto.EstadoResponse;
import com.fabrica.simulacion.dto.SolicitudInicio;
import com.fabrica.simulacion.service.FabricaSimulacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*")
public class SimulacionController {

    private final FabricaSimulacion fabrica;

    public SimulacionController(FabricaSimulacion fabrica) { this.fabrica = fabrica; }

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, Object>> iniciar(@RequestBody(required = false) SolicitudInicio solicitud) {
        try {
            if (solicitud == null) solicitud = new SolicitudInicio();
            fabrica.iniciar(solicitud);
            return ResponseEntity.ok(Map.of("mensaje", "Simulación iniciada"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/detener")
    public ResponseEntity<Map<String, Object>> detener() {
        fabrica.detener();
        return ResponseEntity.ok(Map.of("mensaje", "Simulación detenida"));
    }

    @PostMapping("/reiniciar")
    public ResponseEntity<Map<String, Object>> reiniciar() {
        fabrica.reiniciar();
        return ResponseEntity.ok(Map.of("mensaje", "Sistema reiniciado"));
    }

    @GetMapping("/estado")
    public EstadoResponse estado() {
        return fabrica.estado();
    }
}
