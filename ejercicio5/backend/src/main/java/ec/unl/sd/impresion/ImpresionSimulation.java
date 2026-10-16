package ec.unl.sd.impresion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Ejercicio 5 - Centro de impresión: DISEÑO -> REVISIÓN -> IMPRESIÓN.
 *
 * Tres semáforos, uno por etapa:
 *   turnoDiseno = 1, turnoRevision = 0, turnoImpresion = 0
 * Cada etapa es un hilo que repite {@code docs} veces:
 *   wait(turno_propio) -> trabajo -> signal(turno_siguiente)
 * Impresión habilita de nuevo a Diseño, cerrando el ciclo.
 */
public class ImpresionSimulation {

    public enum Etapa { DISENO, REVISION, IMPRESION }

    /** matriz[doc][etapa]: 0 = pendiente, 1 = en curso, 2 = hecho. */
    public record Snapshot(boolean corriendo, boolean terminado, boolean bloqueado,
                           int documentos, Etapa omitir, int[][] matriz,
                           List<String> secuencia, boolean ordenCorrecto,
                           long tiempoMs, List<String> eventos) {}

    private static final String[] PREFIJO = {"D", "R", "I"};
    private static final String[] NOMBRE = {"Diseño", "Revisión", "Impresión"};
    private static final String[] VERBO = {"prepara", "verifica", "imprime"};

    private final int docs;
    private final Etapa omitir;          // etapa cuyo signal() se elimina (null = ninguna)
    private final long msEtapa;
    private final long umbralMs;         // sin progreso durante este tiempo => bloqueado

    private final Semaphore turnoDiseno = new Semaphore(1);
    private final Semaphore turnoRevision = new Semaphore(0);
    private final Semaphore turnoImpresion = new Semaphore(0);

    private final ExecutorService pool = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private final CountDownLatch terminadoLatch = new CountDownLatch(1);

    // Estado compartido (UI + verificación), protegido por 'lock'
    private final Object lock = new Object();
    private final int[][] matriz;
    private final List<String> secuencia = new ArrayList<>();
    private final List<String> eventos = new ArrayList<>();
    private boolean terminado;
    private long inicioNs;
    private long ultimoProgresoNs;
    private long finNs;

    public ImpresionSimulation(int docs, Etapa omitir, long msEtapa) {
        this.docs = docs;
        this.omitir = omitir;
        this.msEtapa = msEtapa;
        this.umbralMs = Math.max(1500, msEtapa * 3);
        this.matriz = new int[docs][3];
    }

    public void start() {
        synchronized (lock) {
            inicioNs = System.nanoTime();
            ultimoProgresoNs = inicioNs;
        }
        pool.submit(() -> proceso(0, turnoDiseno, turnoRevision));
        pool.submit(() -> proceso(1, turnoRevision, turnoImpresion));
        pool.submit(() -> proceso(2, turnoImpresion, turnoDiseno));
        pool.shutdown();
    }

    /** PROCEDIMIENTO diseno() / revision() / impresion(): REPETIR docs VECES */
    private void proceso(int e, Semaphore espera, Semaphore habilita) {
        try {
            for (int d = 1; d <= docs; d++) {
                espera.acquire();                               // wait(turno_propio)
                iniciar(e, d);
                Thread.sleep(msEtapa);                          // crear/revisar/imprimir_documento()
                terminar(e, d);
                if (omitir == Etapa.values()[e]) {
                    registrar("⚠ " + NOMBRE[e] + " NO ejecuta su signal() (documento " + d + ")");
                } else {
                    habilita.release();                         // signal(turno_siguiente)
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void iniciar(int e, int doc) {
        synchronized (lock) {
            matriz[doc - 1][e] = 1;
            secuencia.add(PREFIJO[e] + doc);
            ultimoProgresoNs = System.nanoTime();
            log(NOMBRE[e] + " " + VERBO[e] + " documento " + doc);
        }
    }

    private void terminar(int e, int doc) {
        synchronized (lock) {
            matriz[doc - 1][e] = 2;
            ultimoProgresoNs = System.nanoTime();
            log("   ✔ " + NOMBRE[e] + " terminó documento " + doc);
            if (e == 2 && doc == docs) {
                terminado = true;
                finNs = ultimoProgresoNs;
                terminadoLatch.countDown();
            }
        }
    }

    private void registrar(String msg) {
        synchronized (lock) { log(msg); }
    }

    private void log(String msg) {          // llamar con 'lock' tomado
        long ms = (System.nanoTime() - inicioNs) / 1_000_000;
        eventos.add(String.format("[%5d ms] %s", ms, msg));
    }

    /** El orden correcto es D1,R1,I1,D2,R2,I2,... */
    private boolean ordenCorrecto() {
        for (int i = 0; i < secuencia.size(); i++) {
            String esperado = PREFIJO[i % 3] + (i / 3 + 1);
            if (!secuencia.get(i).equals(esperado)) return false;
        }
        return true;
    }

    public void stop() {
        pool.shutdownNow();                  // interrumpe los hilos bloqueados en acquire()
    }

    public boolean await(long timeoutMs) throws InterruptedException {
        return terminadoLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public Snapshot snapshot() {
        synchronized (lock) {
            long ahora = System.nanoTime();
            boolean bloqueado = !terminado && (ahora - ultimoProgresoNs) / 1_000_000 > umbralMs;
            long fin = terminado ? finNs : (bloqueado ? ultimoProgresoNs : ahora);
            int[][] copia = new int[docs][];
            for (int i = 0; i < docs; i++) copia[i] = matriz[i].clone();
            return new Snapshot(!terminado && !bloqueado, terminado, bloqueado, docs, omitir,
                    copia, new ArrayList<>(secuencia), ordenCorrecto(),
                    (fin - inicioNs) / 1_000_000, new ArrayList<>(eventos));
        }
    }
}
