package com.fabrica.simulacion.service;

import com.fabrica.simulacion.core.Almacen;
import com.fabrica.simulacion.core.ConfiguracionSimulacion;
import com.fabrica.simulacion.core.Consumidor;
import com.fabrica.simulacion.core.Productor;
import com.fabrica.simulacion.dto.EstadoResponse;
import com.fabrica.simulacion.dto.HiloInfo;
import com.fabrica.simulacion.dto.SolicitudInicio;
import com.fabrica.simulacion.factory.HiloFactory;
import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.observer.BusEventos;
import com.fabrica.simulacion.observer.PublicadorMetricas;
import com.fabrica.simulacion.observer.RegistroMemoriaEventos;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Orquestador de la simulación: crea el almacén, hilos y supervisa el ciclo de vida. */
@Service
public class FabricaSimulacion {

    private final BusEventos bus;
    private final RegistroMemoriaEventos registro;
    private final PublicadorMetricas publicadorMetricas;

    private final Object cerrojoCicloDeVida = new Object();

    private ConfiguracionSimulacion configuracion;
    private Almacen almacen;
    private List<Thread> hilos = List.of();
    private List<Productor> productores = List.of();
    private List<Consumidor> consumidores = List.of();
    private volatile boolean enEjecucion = false;
    private volatile boolean finalizada = false;

    public FabricaSimulacion(BusEventos bus, RegistroMemoriaEventos registro, PublicadorMetricas publicadorMetricas) {
        this.bus = bus;
        this.registro = registro;
        this.publicadorMetricas = publicadorMetricas;
    }

    public void iniciar(SolicitudInicio solicitud) {
        synchronized (cerrojoCicloDeVida) {
            if (enEjecucion) {
                throw new IllegalStateException("Ya existe una simulación en ejecución. Deténgala antes de iniciar otra.");
            }

            registro.limpiar();
            publicadorMetricas.reiniciarMetricas();

            this.configuracion = new ConfiguracionSimulacion.Builder()
                    .numProductores(solicitud.getNumProductores())
                    .numConsumidores(solicitud.getNumConsumidores())
                    .iteraciones(solicitud.getIteraciones())
                    .capacidad(solicitud.getCapacidad())
                    .inicialEspacios(solicitud.getInicialEspacios())
                    .inicialElementos(solicitud.getInicialElementos())
                    .estrategia(solicitud.getEstrategia())
                    .build();

            this.almacen = new Almacen(
                    configuracion.getCapacidad(),
                    configuracion.getInicialEspacios(),
                    configuracion.getInicialElementos(),
                    bus);

            bus.emitir(EventoSimulacion.inicio(configuracion.resumen()));

            List<Thread> nuevosHilos = new ArrayList<>();
            List<Productor> nuevosProductores = new ArrayList<>();
            List<Consumidor> nuevosConsumidores = new ArrayList<>();

            for (int i = 1; i <= configuracion.getNumProductores(); i++) {   // PARA i = 1 HASTA 2
                Productor logica = new Productor(i, almacen, configuracion, bus);
                nuevosProductores.add(logica);
                nuevosHilos.add(HiloFactory.crearProductor(i, logica));
            }
            for (int i = 1; i <= configuracion.getNumConsumidores(); i++) {  // PARA i = 1 HASTA 2
                Consumidor logica = new Consumidor(i, almacen, configuracion, bus);
                nuevosConsumidores.add(logica);
                nuevosHilos.add(HiloFactory.crearConsumidor(i, logica));
            }

            this.productores = nuevosProductores;
            this.consumidores = nuevosConsumidores;
            this.hilos = nuevosHilos;
            this.finalizada = false;
            this.enEjecucion = true;

            nuevosHilos.forEach(Thread::start);
        }
    }

    public void detener() {
        synchronized (cerrojoCicloDeVida) {
            if (!enEjecucion) return;
            hilos.forEach(Thread::interrupt);
            enEjecucion = false;
            finalizada = true;
            bus.emitir(EventoSimulacion.info("SISTEMA", "Simulación detenida manualmente"));
        }
    }

    public void reiniciar() {
        synchronized (cerrojoCicloDeVida) {
            if (enEjecucion) {
                hilos.forEach(Thread::interrupt);
                enEjecucion = false;
                finalizada = true;
            }
            registro.limpiar();
            publicadorMetricas.reiniciarMetricas();
            almacen = null;
            hilos = List.of();
            productores = List.of();
            consumidores = List.of();
            finalizada = false;
            bus.emitir(EventoSimulacion.info("SISTEMA", "Sistema reiniciado"));
        }
    }

    public EstadoResponse estado() {
        synchronized (cerrojoCicloDeVida) {
            if (enEjecucion) verificarFinalizacion();

            EstadoResponse r = new EstadoResponse();
            r.setEnEjecucion(enEjecucion);
            r.setFinalizada(finalizada);

            if (almacen != null) {
                r.setCapacidad(almacen.getCapacidad());
                r.setNivelAlmacen(almacen.getNivel());
                r.setAlmacen(almacen.contenido());
            } else {
                r.setCapacidad(0);
                r.setNivelAlmacen(0);
                r.setAlmacen(List.of());
            }

            r.setTotalProducido(registro.getTotalProducidas());
            r.setTotalConsumido(registro.getTotalConsumidas());
            r.setTotalAnomalias(registro.getTotalAnomalias());

            List<HiloInfo> infoHilos = new ArrayList<>();
            productores.forEach(p -> infoHilos.add(
                    new HiloInfo(p.getNombre(), "PRODUCTOR", p.getProcesadas(), estadoDe(p.getHilo()))));
            consumidores.forEach(c -> infoHilos.add(
                    new HiloInfo(c.getNombre(), "CONSUMIDOR", c.getProcesadas(), estadoDe(c.getHilo()))));
            r.setHilos(infoHilos);

            return r;
        }
    }

    private String estadoDe(Thread hilo) {
        return hilo == null ? "NUEVO" : hilo.getState().name();
    }

    private void verificarFinalizacion() {
        boolean hayVivos = hilos.stream().anyMatch(Thread::isAlive);
        if (!hayVivos) {
            enEjecucion = false;
            finalizada = true;
            bus.emitir(EventoSimulacion.fin(String.format(
                    "Simulación finalizada → producidas=%d, consumidas=%d, anomalías=%d",
                    registro.getTotalProducidas(), registro.getTotalConsumidas(), registro.getTotalAnomalias())));
        }
    }
}
