package com.fabrica.simulacion.observer;

import com.fabrica.simulacion.model.EventoSimulacion;

/** Sujeto observable del patrón Observer. */
public interface GestorEventos {
    void emitir(EventoSimulacion evento);
}
