package ec.unl.sd.parking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParkingSimulationTest {

    private ParkingSimulation.Snapshot correr(int capacidad) throws Exception {
        var sim = new ParkingSimulation(capacidad, 10, 50, 150);
        sim.start();
        assertTrue(sim.await(15_000), "La simulación no terminó (¿bloqueo permanente?)");
        return sim.snapshot();
    }

    @Test
    void conSemaforoEn4NuncaHayMasDe4Dentro() throws Exception {
        for (int i = 0; i < 20; i++) {              // repetir para detectar condiciones de carrera
            var s = correr(4);
            assertTrue(s.maximoDentro() <= 4);
            assertEquals(10, s.finalizados());
            assertEquals(0, s.dentro());
            assertTrue(s.cumple());
        }
    }

    @Test
    void conSemaforoEn1EntraUnoALaVez() throws Exception {
        assertEquals(1, correr(1).maximoDentro());
    }

    @Test
    void conSemaforoEn10SeViolaElLimiteDe4() throws Exception {
        var s = correr(10);
        assertTrue(s.maximoDentro() > 4);
        assertFalse(s.cumple());
    }
}
