package com.fabrica.simulacion.core;

/** Patrón Strategy: familia de algoritmos para simular tiempos de fabricación/ensamblaje. */
public interface EstrategiaTiempos {
    long tiempoFabricacionMs();
    long tiempoEnsamblajeMs();
}
