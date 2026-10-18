package com.fabrica.simulacion.core;

import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.model.Pieza;
import com.fabrica.simulacion.observer.GestorEventos;

import java.util.concurrent.atomic.AtomicInteger;

/** PROCEDIMIENTO consumidor(id) del pseudocódigo. */
public class Consumidor implements Runnable {

    private final String nombre;
    private final Almacen almacen;
    private final int iteraciones;
    private final EstrategiaTiempos tiempos;
    private final GestorEventos gestor;
    private final AtomicInteger procesadas = new AtomicInteger(0);
    private volatile Thread hilo;

    public Consumidor(int id, Almacen almacen, ConfiguracionSimulacion config, GestorEventos gestor) {
        this.nombre = "Consumidor-" + id;
        this.almacen = almacen;
        this.iteraciones = config.getIteraciones();
        this.tiempos = config.getTiempos();
        this.gestor = gestor;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < iteraciones; i++) {          // REPETIR 5 VECES
                Pieza pieza = almacen.retirar(nombre);       // wait/lock/extraer/unlock/signal
                if (pieza == null) continue;                 // anomalía por mala inicialización
                procesadas.incrementAndGet();
                Thread.sleep(tiempos.tiempoEnsamblajeMs());  // ensamblar(pieza)
            }
            gestor.emitir(EventoSimulacion.info(nombre, "Terminó: ensambló " + procesadas.get() + " pieza(s)"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            gestor.emitir(EventoSimulacion.interrupcion(nombre,
                    "Interrumpido mientras esperaba piezas del almacén"));
        }
    }

    public String getNombre() { return nombre; }
    public int getProcesadas() { return procesadas.get(); }
    public Thread getHilo() { return hilo; }
    public void setHilo(Thread hilo) { this.hilo = hilo; }
}
