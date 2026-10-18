package com.sistemasdistribuidos.cajero;

/** Se lanza cuando se pide una simulación mientras otra todavía se está ejecutando. */
public class SimulacionEnCursoException extends RuntimeException {

    public SimulacionEnCursoException() {
        super("Ya hay una simulación en ejecución. Espera a que termine e inténtalo de nuevo.");
    }
}
