package com.fabrica.simulacion.observer;

import com.fabrica.simulacion.model.EventoSimulacion;

public interface ObservadorEventos {
    void onEvento(EventoSimulacion evento);
}
