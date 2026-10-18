package com.fabrica.simulacion.factory;

import com.fabrica.simulacion.core.Consumidor;
import com.fabrica.simulacion.core.Productor;

/** Fábrica de hilos con nombres identificables (crear_proceso(productor/consumidor, i)). */
public class HiloFactory {

    public static Thread crearProductor(int id, Productor logica) {
        Thread hilo = new Thread(logica, "Productor-" + id);
        hilo.setDaemon(false);
        logica.setHilo(hilo);
        return hilo;
    }

    public static Thread crearConsumidor(int id, Consumidor logica) {
        Thread hilo = new Thread(logica, "Consumidor-" + id);
        hilo.setDaemon(false);
        logica.setHilo(hilo);
        return hilo;
    }
}
