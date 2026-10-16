package ec.unl.sd.parking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Ejercicio 1 - Estacionamiento con semáforo contador.
 *
 * Recurso compartido : los espacios del estacionamiento.
 * Semáforo contador  : {@code espacios}, inicializado en {@code capacidad} (4 en la guía).
 * Cada vehículo es un hilo: acquire() = wait(espacios), release() = signal(espacios).
 */
public class ParkingSimulation {

    /** Espacios reales del estacionamiento según la guía (criterio de verificación). */
    public static final int ESPACIOS_REALES = 4;

    public enum Estado { ESPERANDO, DENTRO, FUERA }

    public record VehiculoDto(int id, Estado estado) {}

    public record Snapshot(boolean corriendo, int capacidad, int totalVehiculos,
                           int dentro, int maximoDentro, int esperando, int finalizados,
                           long tiempoMs, boolean cumple,
                           List<VehiculoDto> vehiculos, List<String> eventos) {}

    private final int capacidad;
    private final int total;
    private final long minMs;
    private final long maxMs;

    private final Semaphore espacios;                 // semáforo contador
    private final ExecutorService pool;
    private final CountDownLatch terminado = new CountDownLatch(1);

    // Estado compartido para la UI y la verificación; se protege con 'lock'.
    private final Object lock = new Object();
    private final Estado[] estados;
    private final List<String> eventos = new ArrayList<>();
    private int dentro;
    private int maximoDentro;
    private int finalizados;
    private long inicioNs;
    private long finNs;

    public ParkingSimulation(int capacidad, int total, long minMs, long maxMs) {
        this.capacidad = capacidad;
        this.total = total;
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.espacios = new Semaphore(capacidad, true);   // true = FIFO (justo)
        this.pool = Executors.newFixedThreadPool(total);
        this.estados = new Estado[total + 1];
    }

    public void start() {
        synchronized (lock) {
            inicioNs = System.nanoTime();
            for (int i = 1; i <= total; i++) estados[i] = Estado.ESPERANDO;
        }
        for (int i = 1; i <= total; i++) {
            final int id = i;
            pool.submit(() -> vehiculo(id));
        }
        pool.shutdown();
    }

    private void vehiculo(int id) {
        try {
            registrar("Vehículo " + id + " llega al estacionamiento");

            espacios.acquire();                         // wait(espacios): se bloquea si no hay espacio
            try {
                synchronized (lock) {
                    dentro++;
                    maximoDentro = Math.max(maximoDentro, dentro);
                    estados[id] = Estado.DENTRO;
                    registrarSinLock("Vehículo " + id + " INGRESA (dentro: " + dentro + "/" + capacidad + ")");
                }

                Thread.sleep(ThreadLocalRandom.current().nextLong(minMs, maxMs + 1));   // permanencia

                synchronized (lock) {
                    dentro--;                           // se decrementa ANTES de liberar el semáforo
                    estados[id] = Estado.FUERA;
                    finalizados++;
                    registrarSinLock("Vehículo " + id + " SALE del estacionamiento");
                    if (finalizados == total) {
                        finNs = System.nanoTime();
                        terminado.countDown();
                    }
                }
            } finally {
                espacios.release();                     // signal(espacios)
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void registrar(String msg) {
        synchronized (lock) { registrarSinLock(msg); }
    }

    private void registrarSinLock(String msg) {
        long ms = (System.nanoTime() - inicioNs) / 1_000_000;
        eventos.add(String.format("[%5d ms] %s", ms, msg));
        if (eventos.size() > 300) eventos.remove(0);
    }

    public void stop() {
        pool.shutdownNow();
    }

    public boolean await(long timeoutMs) throws InterruptedException {
        return terminado.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public Snapshot snapshot() {
        synchronized (lock) {
            List<VehiculoDto> lista = new ArrayList<>();
            int esperando = 0;
            for (int i = 1; i <= total; i++) {
                lista.add(new VehiculoDto(i, estados[i]));
                if (estados[i] == Estado.ESPERANDO) esperando++;
            }
            boolean corriendo = finalizados < total;
            long fin = corriendo ? System.nanoTime() : finNs;
            return new Snapshot(corriendo, capacidad, total, dentro, maximoDentro, esperando,
                    finalizados, (fin - inicioNs) / 1_000_000,
                    maximoDentro <= ESPACIOS_REALES, lista, new ArrayList<>(eventos));
        }
    }
}
