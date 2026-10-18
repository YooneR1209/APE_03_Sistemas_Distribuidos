package com.fabrica.simulacion.model;

import java.util.concurrent.atomic.AtomicLong;

public class Pieza {

    private static final AtomicLong SECUENCIA_GLOBAL = new AtomicLong(0);

    private final long id;
    private final String tipo;
    private final int idProductor;
    private final long fabricadaEnMs;

    public Pieza(long id, String tipo, int idProductor, long fabricadaEnMs) {
        this.id = id;
        this.tipo = tipo;
        this.idProductor = idProductor;
        this.fabricadaEnMs = fabricadaEnMs;
    }

    public static long siguienteId() {
        return SECUENCIA_GLOBAL.incrementAndGet();
    }

    public String etiqueta() {
        return "P-" + id + "·" + tipo;
    }

    public long getId() { return id; }
    public String getTipo() { return tipo; }
    public int getIdProductor() { return idProductor; }
    public long getFabricadaEnMs() { return fabricadaEnMs; }
}
