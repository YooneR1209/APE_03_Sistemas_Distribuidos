package com.fabrica.simulacion.dto;

public class SolicitudInicio {

    private Integer numProductores;
    private Integer numConsumidores;
    private Integer iteraciones;
    private Integer capacidad;
    private Integer inicialEspacios;
    private Integer inicialElementos;
    private String estrategia;

    public int getNumProductores() { return numProductores != null ? numProductores : 2; }
    public int getNumConsumidores() { return numConsumidores != null ? numConsumidores : 2; }
    public int getIteraciones() { return iteraciones != null ? iteraciones : 5; }
    public int getCapacidad() { return capacidad != null ? capacidad : 5; }
    public int getInicialEspacios() { return inicialEspacios != null ? inicialEspacios : -1; }
    public int getInicialElementos() { return inicialElementos != null ? inicialElementos : 0; }
    public String getEstrategia() { return estrategia != null ? estrategia : "ALEATORIA"; }

    public void setNumProductores(Integer v) { this.numProductores = v; }
    public void setNumConsumidores(Integer v) { this.numConsumidores = v; }
    public void setIteraciones(Integer v) { this.iteraciones = v; }
    public void setCapacidad(Integer v) { this.capacidad = v; }
    public void setInicialEspacios(Integer v) { this.inicialEspacios = v; }
    public void setInicialElementos(Integer v) { this.inicialElementos = v; }
    public void setEstrategia(String v) { this.estrategia = v; }
}
