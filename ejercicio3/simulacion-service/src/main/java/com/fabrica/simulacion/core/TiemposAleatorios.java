package com.fabrica.simulacion.core;

import java.util.concurrent.ThreadLocalRandom;

public class TiemposAleatorios implements EstrategiaTiempos {
    @Override public long tiempoFabricacionMs() { return ThreadLocalRandom.current().nextLong(200, 700); }
    @Override public long tiempoEnsamblajeMs() { return ThreadLocalRandom.current().nextLong(300, 900); }
}
