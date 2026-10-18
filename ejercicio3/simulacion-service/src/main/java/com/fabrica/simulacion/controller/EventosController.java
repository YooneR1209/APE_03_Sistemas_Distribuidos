package com.fabrica.simulacion.controller;

import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.observer.RegistroMemoriaEventos;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventosController {

    private final RegistroMemoriaEventos registro;

    public EventosController(RegistroMemoriaEventos registro) { this.registro = registro; }

    @GetMapping
    public List<EventoSimulacion> eventos(@RequestParam(defaultValue = "120") int limite) {
        return registro.ultimos(Math.min(limite, 500));
    }
}
