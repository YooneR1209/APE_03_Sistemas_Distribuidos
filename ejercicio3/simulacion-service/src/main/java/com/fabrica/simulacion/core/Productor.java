package com.fabrica.simulacion.core;

import com.fabrica.simulacion.factory.PiezaFactory;
import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.model.Pieza;
import com.fabrica.simulacion.observer.GestorEventos;

import java.util.concurrent.atomic.AtomicInteger;

/** PROCEDIMIENTO productor(id) del pseudocódigo. */
public class Productor implements Runnable {

    private final int id;
    private final String nombre;
    private final Almacen almacen;
    private final int iteraciones;
    private final EstrategiaTiempos tiempos;
    private final PiezaFactory piezaFactory = new PiezaFactory();
    private final GestorEventos gestor;
    private final AtomicInteger procesadas = new AtomicInteger(0);
    private volatile Thread hilo;

    public Productor(int id, Almacen almacen, ConfiguracionSimulacion config, GestorEventos gestor) {
        this.id = id;
        this.nombre = "Productor-" + id;
        this.almacen = almacen;
        this.iteraciones = config.getIteraciones();
        this.tiempos = config.getTiempos();
        this.gestor = gestor;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < iteraciones; i++) {          // REPETIR 5 VECES
                Thread.sleep(tiempos.tiempoFabricacionMs()); // pieza = fabricar_pieza()
                Pieza pieza = piezaFactory.crear(id);
                almacen.colocar(pieza, nombre);              // wait/lock/insertar/unlock/signal
                procesadas.incrementAndGet();
            }
            gestor.emitir(EventoSimulacion.info(nombre, "Terminó: fabricó " + procesadas.get() + " pieza(s)"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            gestor.emitir(EventoSimulacion.interrupcion(nombre,
                    "Interrumpido mientras esperaba un espacio del almacén"));
        }
    }

    public String getNombre() { return nombre; }
    public int getProcesadas() { return procesadas.get(); }
    public Thread getHilo() { return hilo; }
    public void setHilo(Thread hilo) { this.hilo = hilo; }
}
