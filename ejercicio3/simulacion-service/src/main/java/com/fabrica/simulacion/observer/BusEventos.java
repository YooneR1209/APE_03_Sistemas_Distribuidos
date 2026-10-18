package com.fabrica.simulacion.observer;

import com.fabrica.simulacion.model.EventoSimulacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Sujeto concreto: notifica a todos los observadores. Bean singleton de Spring. */
@Component
public class BusEventos implements GestorEventos {

    private final List<ObservadorEventos> observadores;
    private final AtomicLong secuencia = new AtomicLong(0);

    public BusEventos(List<ObservadorEventos> observadores) {
        this.observadores = observadores;
    }

    @Override
    public void emitir(EventoSimulacion evento) {
        evento.setId(secuencia.incrementAndGet());
        for (ObservadorEventos observador : observadores) {
            observador.onEvento(evento);
        }
    }
}
