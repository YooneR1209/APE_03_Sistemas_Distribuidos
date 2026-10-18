package com.fabrica.simulacion.core;

public class TiemposFijos implements EstrategiaTiempos {
    @Override public long tiempoFabricacionMs() { return 400; }
    @Override public long tiempoEnsamblajeMs() { return 500; }
}
