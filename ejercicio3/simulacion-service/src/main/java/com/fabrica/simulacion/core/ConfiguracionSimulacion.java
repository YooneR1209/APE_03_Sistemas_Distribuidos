package com.fabrica.simulacion.core;

/** Patrón Builder: construcción paso a paso de una configuración inmutable. */
public class ConfiguracionSimulacion {

    private final int numProductores;
    private final int numConsumidores;
    private final int iteraciones;
    private final int capacidad;
    private final int inicialEspacios;
    private final int inicialElementos;
    private final EstrategiaTiempos tiempos;

    private ConfiguracionSimulacion(Builder b) {
        this.numProductores = b.numProductores;
        this.numConsumidores = b.numConsumidores;
        this.iteraciones = b.iteraciones;
        this.capacidad = b.capacidad;
        this.inicialEspacios = b.inicialEspacios;
        this.inicialElementos = b.inicialElementos;
        this.tiempos = b.estrategia;
    }

    public int getNumProductores() { return numProductores; }
    public int getNumConsumidores() { return numConsumidores; }
    public int getIteraciones() { return iteraciones; }
    public int getCapacidad() { return capacidad; }
    public int getInicialEspacios() { return inicialEspacios; }
    public int getInicialElementos() { return inicialElementos; }
    public EstrategiaTiempos getTiempos() { return tiempos; }

    public String resumen() {
        return "Productores=" + numProductores + ", Consumidores=" + numConsumidores
                + ", Iteraciones=" + iteraciones + ", Capacidad=" + capacidad
                + ", espacios=" + inicialEspacios + ", elementos=" + inicialElementos;
    }

    public static class Builder {
        private int numProductores = 2;
        private int numConsumidores = 2;
        private int iteraciones = 5;
        private int capacidad = 5;
        private int inicialEspacios = -1;   // -1 => se toma la capacidad (valor del enunciado)
        private int inicialElementos = 0;
        private EstrategiaTiempos estrategia = new TiemposAleatorios();

        public Builder numProductores(int v) { if (v > 0) this.numProductores = v; return this; }
        public Builder numConsumidores(int v) { if (v > 0) this.numConsumidores = v; return this; }
        public Builder iteraciones(int v) { if (v > 0) this.iteraciones = v; return this; }
        public Builder capacidad(int v) { if (v > 0) this.capacidad = v; return this; }
        public Builder inicialEspacios(int v) { if (v >= 0) this.inicialEspacios = v; return this; }
        public Builder inicialElementos(int v) { if (v >= 0) this.inicialElementos = v; return this; }
        public Builder estrategia(String nombre) {
            this.estrategia = "FIJA".equalsIgnoreCase(nombre) ? new TiemposFijos() : new TiemposAleatorios();
            return this;
        }

        public ConfiguracionSimulacion build() {
            if (inicialEspacios < 0) inicialEspacios = capacidad;
            return new ConfiguracionSimulacion(this);
        }
    }
}
