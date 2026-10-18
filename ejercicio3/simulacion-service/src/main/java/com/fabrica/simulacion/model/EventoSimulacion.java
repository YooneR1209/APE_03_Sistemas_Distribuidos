package com.fabrica.simulacion.model;

public class EventoSimulacion {

    private long id;
    private long timestamp;
    private TipoEvento tipo;
    private String actor;
    private String mensaje;
    private int nivelAlmacen;
    private int capacidad;

    public EventoSimulacion() { }

    public EventoSimulacion(TipoEvento tipo, String actor, String mensaje, int nivelAlmacen, int capacidad) {
        this.tipo = tipo;
        this.actor = actor;
        this.mensaje = mensaje;
        this.nivelAlmacen = nivelAlmacen;
        this.capacidad = capacidad;
        this.timestamp = System.currentTimeMillis();
    }

    public static EventoSimulacion inicio(String resumen) {
        return new EventoSimulacion(TipoEvento.INICIO, "SISTEMA", "Simulación iniciada → " + resumen, -1, -1);
    }

    public static EventoSimulacion producido(String actor, Pieza p, int nivel, int capacidad) {
        return new EventoSimulacion(TipoEvento.PRODUCIDO, actor,
                "Agregó la pieza " + p.etiqueta() + " al almacén", nivel, capacidad);
    }

    public static EventoSimulacion consumido(String actor, Pieza p, int nivel, int capacidad) {
        return new EventoSimulacion(TipoEvento.CONSUMIDO, actor,
                "Retiró la pieza " + p.etiqueta() + " del almacén", nivel, capacidad);
    }

    public static EventoSimulacion anomalia(String actor, String mensaje, int nivel, int capacidad) {
        return new EventoSimulacion(TipoEvento.ANOMALIA, actor, mensaje, nivel, capacidad);
    }

    public static EventoSimulacion info(String actor, String mensaje) {
        return new EventoSimulacion(TipoEvento.INFO, actor, mensaje, -1, -1);
    }

    public static EventoSimulacion interrupcion(String actor, String mensaje) {
        return new EventoSimulacion(TipoEvento.INTERRUPCION, actor, mensaje, -1, -1);
    }

    public static EventoSimulacion fin(String mensaje) {
        return new EventoSimulacion(TipoEvento.FIN, "SISTEMA", mensaje, -1, -1);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public int getNivelAlmacen() { return nivelAlmacen; }
    public void setNivelAlmacen(int nivelAlmacen) { this.nivelAlmacen = nivelAlmacen; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
}
