package com.sistemasdistribuidos.cajero;

/**
 * Resultado de la operación que realizó un proceso.
 *
 * @param tipo         "RETIRO" o "DEPOSITO"
 * @param realizada    false cuando un retiro se rechazó por saldo insuficiente
 * @param saldoDespues saldo que dejó el proceso al terminar su operación
 */
public record Operacion(String proceso, String tipo, int monto, boolean realizada, int saldoDespues) {
}
