package com.sistemasdistribuidos.cajero;

/**
 * Una línea del registro de la simulación.
 *
 * @param orden    número de evento (1, 2, 3...) en el orden en que ocurrió
 * @param tiempoMs milisegundos transcurridos desde que empezó la simulación
 * @param proceso  "Proceso 1", "Proceso 2" o "Proceso 3"
 * @param mensaje  qué hizo el proceso
 * @param saldo    saldo asociado al evento (null si el evento no lo tiene)
 */
public record Evento(int orden, double tiempoMs, String proceso, String mensaje, Integer saldo) {
}
