package com.sistemasdistribuidos.cajero;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cuenta bancaria compartida por los tres procesos.
 * El recurso crítico es el campo {@code saldo}.
 */
public class CuentaBancaria {

    public static final int SALDO_INICIAL = 1000;

    // Pausa SOLO para la demostración sin mutex: deja una ventana entre leer y escribir
    // el saldo. No garantiza que la condición de carrera ocurra; solo la hace posible.
    private static final long PAUSA_DEMO_MS = 50;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile int saldo = SALDO_INICIAL;

    // Registro de eventos. Su propio candado solo protege la lista, nunca el saldo.
    private final Object registroLock = new Object();
    private final List<Evento> eventos = new ArrayList<>();
    private long inicioNanos = System.nanoTime();

    public int getSaldo() {
        return saldo;
    }

    /** Deja la cuenta en $1000 y borra los eventos de la simulación anterior. */
    public void reiniciar() {
        synchronized (registroLock) {
            eventos.clear();
            inicioNanos = System.nanoTime();
        }
        saldo = SALDO_INICIAL;
    }

    public List<Evento> getEventos() {
        synchronized (registroLock) {
            return new ArrayList<>(eventos);
        }
    }

    // ---------------------------------------------------------------
    // CON MUTEX: la comprobación y la modificación del saldo ocurren
    // dentro de la misma sección crítica.
    // ---------------------------------------------------------------

    public Operacion retirar(String proceso, int monto) {
        validarMonto(monto);
        lock.lock();
        try {
            registrar(proceso, "Intenta retirar $" + monto, saldo);
            if (saldo >= monto) {
                saldo = saldo - monto;
                registrar(proceso, "Retiro realizado de $" + monto, saldo);
                return new Operacion(proceso, "RETIRO", monto, true, saldo);
            }
            registrar(proceso, "Saldo insuficiente para retirar $" + monto, saldo);
            return new Operacion(proceso, "RETIRO", monto, false, saldo);
        } finally {
            lock.unlock();
        }
    }

    public Operacion depositar(String proceso, int monto) {
        validarMonto(monto);
        lock.lock();
        try {
            registrar(proceso, "Intenta depositar $" + monto, saldo);
            saldo = saldo + monto;
            registrar(proceso, "Depósito realizado de $" + monto, saldo);
            return new Operacion(proceso, "DEPOSITO", monto, true, saldo);
        } finally {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------------
    // SIN MUTEX (solo educativo): lee, hace una pausa y escribe sin
    // bloqueo. Otro proceso puede cambiar el saldo entre la lectura y
    // la escritura, y esa actualización se pierde.
    // ---------------------------------------------------------------

    public Operacion retirarSinMutex(String proceso, int monto) throws InterruptedException {
        validarMonto(monto);
        registrar(proceso, "Intenta retirar $" + monto + " (sin mutex)", null);
        int saldoLeido = saldo;
        registrar(proceso, "Leyó el saldo: $" + saldoLeido, saldoLeido);
        Thread.sleep(PAUSA_DEMO_MS);
        if (saldoLeido >= monto) {
            saldo = saldoLeido - monto;
            registrar(proceso, "Escribió el saldo: $" + saldo + " (retiro de $" + monto + ")", saldo);
            return new Operacion(proceso, "RETIRO", monto, true, saldo);
        }
        registrar(proceso, "Saldo insuficiente para retirar $" + monto, saldoLeido);
        return new Operacion(proceso, "RETIRO", monto, false, saldoLeido);
    }

    public Operacion depositarSinMutex(String proceso, int monto) throws InterruptedException {
        validarMonto(monto);
        registrar(proceso, "Intenta depositar $" + monto + " (sin mutex)", null);
        int saldoLeido = saldo;
        registrar(proceso, "Leyó el saldo: $" + saldoLeido, saldoLeido);
        Thread.sleep(PAUSA_DEMO_MS);
        saldo = saldoLeido + monto;
        registrar(proceso, "Escribió el saldo: $" + saldo + " (depósito de $" + monto + ")", saldo);
        return new Operacion(proceso, "DEPOSITO", monto, true, saldo);
    }

    // ---------------------------------------------------------------

    private void validarMonto(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
    }

    private void registrar(String proceso, String mensaje, Integer saldoEvento) {
        synchronized (registroLock) {
            double ms = (System.nanoTime() - inicioNanos) / 1_000_000.0;
            ms = Math.round(ms * 100) / 100.0;
            eventos.add(new Evento(eventos.size() + 1, ms, proceso, mensaje, saldoEvento));
        }
    }
}
