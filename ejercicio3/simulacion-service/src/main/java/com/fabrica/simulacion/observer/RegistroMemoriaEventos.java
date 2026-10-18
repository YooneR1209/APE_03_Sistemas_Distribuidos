package com.fabrica.simulacion.observer;

import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.model.TipoEvento;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Observador 1: bitácora en memoria (últimos 500 eventos) + contadores locales. */
@Component
public class RegistroMemoriaEventos implements ObservadorEventos {

    private static final int MAX_EVENTOS = 500;

    private final Deque<EventoSimulacion> eventos = new ArrayDeque<>();
    private final AtomicLong producidas = new AtomicLong();
    private final AtomicLong consumidas = new AtomicLong();
    private final AtomicLong anomalias = new AtomicLong();

    @Override
    public synchronized void onEvento(EventoSimulacion evento) {
        switch (evento.getTipo()) {
            case PRODUCIDO -> producidas.incrementAndGet();
            case CONSUMIDO -> consumidas.incrementAndGet();
            case ANOMALIA -> anomalias.incrementAndGet();
            default -> { }
        }
        if (eventos.size() >= MAX_EVENTOS) eventos.pollFirst();
        eventos.addLast(evento);
    }

    /** Últimos eventos, del más reciente al más antiguo. */
    public synchronized List<EventoSimulacion> ultimos(int limite) {
        List<EventoSimulacion> salida = new ArrayList<>();
        Iterator<EventoSimulacion> it = eventos.descendingIterator();
        while (it.hasNext() && salida.size() < limite) salida.add(it.next());
        return salida;
    }

    public synchronized void limpiar() {
        eventos.clear();
        producidas.set(0);
        consumidas.set(0);
        anomalias.set(0);
    }

    public long getTotalProducidas() { return producidas.get(); }
    public long getTotalConsumidas() { return consumidas.get(); }
    public long getTotalAnomalias() { return anomalias.get(); }
}
