package com.sistemasdistribuidos.cajero;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cajero")
@CrossOrigin(origins = "http://localhost:5173")
public class CajeroController {

    private final CajeroService cajeroService;

    public CajeroController(CajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @GetMapping("/saldo")
    public Map<String, Integer> saldo() {
        return Map.of("saldo", cajeroService.consultarSaldo());
    }

    @PostMapping("/simular/con-mutex")
    public ResultadoSimulacion simularConMutex() {
        return cajeroService.ejecutarSimulacion(true);
    }

    @PostMapping("/simular/sin-mutex")
    public ResultadoSimulacion simularSinMutex() {
        return cajeroService.ejecutarSimulacion(false);
    }

    // Ya hay otra simulación corriendo -> 409
    @ExceptionHandler(SimulacionEnCursoException.class)
    public ResponseEntity<Map<String, String>> simulacionEnCurso(SimulacionEnCursoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    // Cualquier otro fallo -> 500 con un mensaje legible
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> errorInesperado(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno: " + e.getMessage()));
    }
}
