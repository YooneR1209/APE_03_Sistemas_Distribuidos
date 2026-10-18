package com.sistemasdistribuidos.cajero;

import java.util.List;

/** Respuesta JSON de una simulación. */
public record ResultadoSimulacion(
        String modo,
        int saldoInicial,
        int saldoFinal,
        int saldoEsperado,
        boolean correcto,
        String verificacion,
        List<Operacion> operaciones,
        List<Evento> eventos) {
}
