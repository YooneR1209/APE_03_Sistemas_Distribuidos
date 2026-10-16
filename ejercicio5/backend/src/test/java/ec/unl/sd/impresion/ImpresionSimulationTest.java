package ec.unl.sd.impresion;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImpresionSimulationTest {

    @Test
    void elOrdenSiempreEsDisenoRevisionImpresion() throws Exception {
        for (int i = 0; i < 10; i++) {
            var sim = new ImpresionSimulation(3, null, 10);
            sim.start();
            assertTrue(sim.await(10_000));
            var s = sim.snapshot();
            assertTrue(s.terminado());
            assertTrue(s.ordenCorrecto());
            assertEquals(List.of("D1", "R1", "I1", "D2", "R2", "I2", "D3", "R3", "I3"), s.secuencia());
        }
    }

    private ImpresionSimulation.Snapshot sinSignal(ImpresionSimulation.Etapa etapa) throws Exception {
        var sim = new ImpresionSimulation(3, etapa, 10);
        sim.start();
        assertFalse(sim.await(2_500), "no debería terminar");
        var s = sim.snapshot();
        sim.stop();
        return s;
    }

    @Test
    void sinSignalDeDisenoSeBloqueaTrasD1() throws Exception {
        var s = sinSignal(ImpresionSimulation.Etapa.DISENO);
        assertTrue(s.bloqueado());
        assertEquals(List.of("D1"), s.secuencia());
    }

    @Test
    void sinSignalDeRevisionSeBloqueaTrasR1() throws Exception {
        var s = sinSignal(ImpresionSimulation.Etapa.REVISION);
        assertTrue(s.bloqueado());
        assertEquals(List.of("D1", "R1"), s.secuencia());
    }

    @Test
    void sinSignalDeImpresionSeBloqueaTrasI1() throws Exception {
        var s = sinSignal(ImpresionSimulation.Etapa.IMPRESION);
        assertTrue(s.bloqueado());
        assertEquals(List.of("D1", "R1", "I1"), s.secuencia());
    }
}
