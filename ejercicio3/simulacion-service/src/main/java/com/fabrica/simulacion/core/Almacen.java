package com.fabrica.simulacion.core;

import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.model.Pieza;
import com.fabrica.simulacion.observer.GestorEventos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Almacén circular compartido (recurso crítico único -> patrón Singleton por ejecución).
 * Cada método traduce EXACTAMENTE el pseudocódigo del enunciado:
 *
 *   wait(espacios)      -> espacios.acquire()
 *   lock(mutex_almacen) -> mutex.lock()
 *   unlock(mutex)       -> mutex.unlock()
 *   signal(elementos)   -> elementos.release()
 *   signal(espacios)    -> espacios.release()
 */
public class Almacen {

    private final int capacidad;
    private final Pieza[] buffer;

    private int cabeza = 0;    // índice de extracción (FIFO)
    private int cola = 0;      // índice de inserción
    private int cantidad = 0;  // piezas almacenadas actualmente

    private final Semaphore espacios;
    private final Semaphore elementos;
    private final ReentrantLock mutex = new ReentrantLock(true); // cerrojo justo

    private final GestorEventos gestor;

    public Almacen(int capacidad, int inicialEspacios, int inicialElementos, GestorEventos gestor) {
        this.capacidad = capacidad;
        this.buffer = new Pieza[capacidad];
        this.espacios = new Semaphore(inicialEspacios);
        this.elementos = new Semaphore(inicialElementos);
        this.gestor = gestor;
    }

    /** PRODUCTOR: insertar(almacen, pieza) */
    public boolean colocar(Pieza pieza, String actor) throws InterruptedException {
        espacios.acquire();                     // wait(espacios): espera un espacio disponible
        mutex.lock();                           // lock(mutex_almacen)
        boolean insertada = false;
        try {
            if (cantidad >= capacidad) {
                // Solo posible si 'espacios' fue mal inicializado (experimento de análisis)
                gestor.emitir(EventoSimulacion.anomalia(actor,
                        "ANOMALÍA: intentó insertar con almacén LLENO (semáforo 'espacios' mal inicializado)",
                        cantidad, capacidad));
            } else {
                buffer[cola] = pieza;
                cola = (cola + 1) % capacidad;
                cantidad++;
                insertada = true;
                gestor.emitir(EventoSimulacion.producido(actor, pieza, cantidad, capacidad));
            }
        } finally {
            mutex.unlock();                     // unlock(mutex_almacen)
        }
        if (insertada) {
            elementos.release();                // signal(elementos): indica que hay una pieza nueva
        }
        return insertada;
    }

    /** CONSUMIDOR: pieza = extraer(almacen) */
    public Pieza retirar(String actor) throws InterruptedException {
        elementos.acquire();                    // wait(elementos): espera hasta que exista una pieza
        mutex.lock();                           // lock(mutex_almacen)
        Pieza pieza = null;
        try {
            if (cantidad == 0) {
                // Solo posible si 'elementos' fue mal inicializado (experimento de análisis)
                gestor.emitir(EventoSimulacion.anomalia(actor,
                        "ANOMALÍA: intentó retirar con almacén VACÍO (semáforo 'elementos' mal inicializado)",
                        0, capacidad));
            } else {
                pieza = buffer[cabeza];
                buffer[cabeza] = null;
                cabeza = (cabeza + 1) % capacidad;
                cantidad--;
                gestor.emitir(EventoSimulacion.consumido(actor, pieza, cantidad, capacidad));
            }
        } finally {
            mutex.unlock();                     // unlock(mutex_almacen)
        }
        if (pieza != null) {
            espacios.release();                 // signal(espacios): indica que hay un espacio disponible
        }
        return pieza;
    }

    /** Copia del contenido actual, tomada bajo el mutex (para la interfaz). */
    public List<String> contenido() {
        mutex.lock();
        try {
            List<String> salida = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                salida.add(buffer[(cabeza + i) % capacidad].etiqueta());
            }
            return salida;
        } finally {
            mutex.unlock();
        }
    }

    public int getNivel() {
        mutex.lock();
        try { return cantidad; } finally { mutex.unlock(); }
    }

    public int getCapacidad() { return capacidad; }
}
