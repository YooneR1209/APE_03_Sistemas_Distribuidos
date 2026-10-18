package com.sistemasdistribuidos.cajero;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class CajeroService {

    private static final int RETIRO_PROCESO_1 = 300;
    private static final int RETIRO_PROCESO_2 = 250;
    private static final int DEPOSITO_PROCESO_3 = 200;
    private static final int SALDO_ESPERADO =
            CuentaBancaria.SALDO_INICIAL - RETIRO_PROCESO_1 - RETIRO_PROCESO_2 + DEPOSITO_PROCESO_3; // 650

    private final CuentaBancaria cuenta = new CuentaBancaria();
    private final AtomicBoolean simulacionEnCurso = new AtomicBoolean(false);

    public int consultarSaldo() {
        return cuenta.getSaldo();
    }

    public ResultadoSimulacion ejecutarSimulacion(boolean conMutex) {
        // Solo una simulación a la vez: si ya hay una en curso, se rechaza la nueva.
        if (!simulacionEnCurso.compareAndSet(false, true)) {
            throw new SimulacionEnCursoException();
        }
        try {
            cuenta.reiniciar();
            int saldoInicial = cuenta.getSaldo();

            List<Operacion> operaciones = ejecutarProcesos(conMutex);

            int saldoFinal = cuenta.getSaldo();
            boolean correcto = saldoFinal == SALDO_ESPERADO;
            return new ResultadoSimulacion(
                    conMutex ? "CON_MUTEX" : "SIN_MUTEX",
                    saldoInicial,
                    saldoFinal,
                    SALDO_ESPERADO,
                    correcto,
                    verificar(conMutex, correcto, saldoFinal),
                    operaciones,
                    cuenta.getEventos());
        } finally {
            simulacionEnCurso.set(false);
        }
    }

    /** Lanza los tres procesos en hilos distintos y espera a que terminen. */
    private List<Operacion> ejecutarProcesos(boolean conMutex) {
        // Compuerta de salida: los tres hilos esperan aquí y arrancan a la vez.
        // No sincroniza el saldo (de eso se encarga el mutex); solo iguala la salida.
        CountDownLatch salida = new CountDownLatch(1);

        List<Callable<Operacion>> procesos = List.of(
                proceso(salida, () -> conMutex
                        ? cuenta.retirar("Proceso 1", RETIRO_PROCESO_1)
                        : cuenta.retirarSinMutex("Proceso 1", RETIRO_PROCESO_1)),
                proceso(salida, () -> conMutex
                        ? cuenta.retirar("Proceso 2", RETIRO_PROCESO_2)
                        : cuenta.retirarSinMutex("Proceso 2", RETIRO_PROCESO_2)),
                proceso(salida, () -> conMutex
                        ? cuenta.depositar("Proceso 3", DEPOSITO_PROCESO_3)
                        : cuenta.depositarSinMutex("Proceso 3", DEPOSITO_PROCESO_3)));

        try (ExecutorService hilos = Executors.newFixedThreadPool(3)) {
            List<Future<Operacion>> futuros = new ArrayList<>();
            try {
                for (Callable<Operacion> proceso : procesos) {
                    futuros.add(hilos.submit(proceso));
                }
            } finally {
                salida.countDown();
            }

            List<Operacion> operaciones = new ArrayList<>();
            for (Future<Operacion> futuro : futuros) {
                operaciones.add(futuro.get()); // espera a que termine cada proceso
            }
            return operaciones;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La simulación fue interrumpida", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Un proceso falló: " + e.getCause(), e.getCause());
        }
    }

    private Callable<Operacion> proceso(CountDownLatch salida, Callable<Operacion> operacion) {
        return () -> {
            salida.await();
            return operacion.call();
        };
    }

    private String verificar(boolean conMutex, boolean correcto, int saldoFinal) {
        if (conMutex) {
            return correcto
                    ? "Correcto: el saldo final es $" + saldoFinal + ", igual al esperado ($" + SALDO_ESPERADO + ")."
                    : "Inesperado: el saldo final es $" + saldoFinal + " y se esperaba $" + SALDO_ESPERADO + ".";
        }
        return correcto
                ? "La condición de carrera no se manifestó en esta ejecución: el saldo coincide con el esperado ($"
                  + SALDO_ESPERADO + "). Sin mutex eso no está garantizado; vuelve a ejecutar."
                : "Condición de carrera detectada: el saldo final es $" + saldoFinal + " y se esperaba $"
                  + SALDO_ESPERADO + ". Se perdieron actualizaciones.";
    }
}
