package com.fabrica.simulacion.factory;

import com.fabrica.simulacion.model.Pieza;
import java.util.concurrent.ThreadLocalRandom;

/** Fábrica de piezas: centraliza cómo se crea una pieza. */
public class PiezaFactory {

    private static final String[] TIPOS = {"TORNILLO", "PISTON", "CARCASA", "ENGRAJE", "VALVULA"};

    public Pieza crear(int idProductor) {
        String tipo = TIPOS[ThreadLocalRandom.current().nextInt(TIPOS.length)];
        return new Pieza(Pieza.siguienteId(), tipo, idProductor, System.currentTimeMillis());
    }
}
