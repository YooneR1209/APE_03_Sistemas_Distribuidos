package com.fabrica.simulacion.dto;

import java.util.List;

public class EstadoResponse {
    private boolean enEjecucion;
    private boolean finalizada;
    private int capacidad;
    private int nivelAlmacen;
    private List<String> almacen;
    private long totalProducido;
    private long totalConsumido;
    private long totalAnomalias;
    private List<HiloInfo> hilos;

    public boolean isEnEjecucion() { return enEjecucion; }
    public void setEnEjecucion(boolean v) { this.enEjecucion = v; }
    public boolean isFinalizada() { return finalizada; }
    public void setFinalizada(boolean v) { this.finalizada = v; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int v) { this.capacidad = v; }
    public int getNivelAlmacen() { return nivelAlmacen; }
    public void setNivelAlmacen(int v) { this.nivelAlmacen = v; }
    public List<String> getAlmacen() { return almacen; }
    public void setAlmacen(List<String> v) { this.almacen = v; }
    public long getTotalProducido() { return totalProducido; }
    public void setTotalProducido(long v) { this.totalProducido = v; }
    public long getTotalConsumido() { return totalConsumido; }
    public void setTotalConsumido(long v) { this.totalConsumido = v; }
    public long getTotalAnomalias() { return totalAnomalias; }
    public void setTotalAnomalias(long v) { this.totalAnomalias = v; }
    public List<HiloInfo> getHilos() { return hilos; }
    public void setHilos(List<HiloInfo> v) { this.hilos = v; }
}
