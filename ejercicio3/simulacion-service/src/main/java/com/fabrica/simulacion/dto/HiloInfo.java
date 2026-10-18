package com.fabrica.simulacion.dto;

public class HiloInfo {
    private final String nombre;
    private final String rol;
    private final int procesadas;
    private final String estado;

    public HiloInfo(String nombre, String rol, int procesadas, String estado) {
        this.nombre = nombre;
        this.rol = rol;
        this.procesadas = procesadas;
        this.estado = estado;
    }

    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public int getProcesadas() { return procesadas; }
    public String getEstado() { return estado; }
}
