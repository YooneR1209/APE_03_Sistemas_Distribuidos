package com.puente.backend.controlador;

import com.puente.backend.modelo.Puente;
import com.puente.backend.servicio.PuenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/puente")
public class PuenteController {

    @Autowired
    private PuenteService puenteService;

    @GetMapping("/estado")
    public Puente obtenerEstado() {
        return puenteService.obtenerEstado();
    }

    @PostMapping("/cruzar/norte")
    public String vehiculoNorte(@RequestParam String idVehiculo) {
        try {
            return puenteService.vehiculoNorte(idVehiculo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "El proceso del vehículo " + idVehiculo + " fue interrumpido.";
        }
    }

    @PostMapping("/cruzar/sur")
    public String vehiculoSur(@RequestParam String idVehiculo) {
        try {
            return puenteService.vehiculoSur(idVehiculo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "El proceso del vehículo " + idVehiculo + " fue interrumpido.";
        }
    }
}