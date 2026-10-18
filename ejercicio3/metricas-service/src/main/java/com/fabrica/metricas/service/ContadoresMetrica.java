package com.fabrica.metricas.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ContadoresMetrica {

    private final AtomicLong producidas = new AtomicLong();
    private final AtomicLong consumidas = new AtomicLong();
    private final AtomicLong anomalias = new AtomicLong();
    private volatile long inicioMs = 0;
    private volatile long ultimoEventoMs = 0;

    public void registrar(String tipo) {
        long ahora = System.currentTimeMillis();
        synchronized (this) {
            if (inicioMs == 0) inicioMs = ahora;
            ultimoEventoMs = ahora;
        }
        switch (tipo) {
            case "PRODUCIDO" -> producidas.incrementAndGet();
            case "CONSUMIDO" -> consumidas.incrementAndGet();
            case "ANOMALIA" -> anomalias.incrementAndGet();
            default -> { }
        }
    }

    public Map<String, Object> resumen() {
        long p = producidas.get();
        long c = consumidas.get();
        long duracionSeg = (inicioMs == 0) ? 0 : Math.max(1, (ultimoEventoMs - inicioMs) / 1000);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("producidas", p);
        m.put("consumidas", c);
        m.put("enAlmacen", Math.max(0, p - c));
        m.put("anomalias", anomalias.get());
        m.put("duracionSegundos", duracionSeg);
        m.put("produccionPorSegundo", duracionSeg == 0 ? 0.0 : redondear(p / (double) duracionSeg));
        m.put("consumoPorSegundo", duracionSeg == 0 ? 0.0 : redondear(c / (double) duracionSeg));
        m.put("balance", p - c);   // 0 => TODO lo producido fue consumido (criterio del docente)
        return m;
    }

    public void reiniciar() {
        producidas.set(0);
        consumidas.set(0);
        anomalias.set(0);
        inicioMs = 0;
        ultimoEventoMs = 0;
    }

    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
