package ec.unl.sd.impresion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impresion")
@CrossOrigin(origins = "http://localhost:5174")
public class ImpresionController {

    private ImpresionSimulation actual;

    /** omitir (opcional): DISENO | REVISION | IMPRESION -> elimina el signal() de esa etapa. */
    @PostMapping("/start")
    public synchronized ResponseEntity<?> start(
            @RequestParam(defaultValue = "3") int docs,
            @RequestParam(required = false) ImpresionSimulation.Etapa omitir) {

        if (docs < 1 || docs > 10) {
            return ResponseEntity.badRequest().body("docs debe estar entre 1 y 10");
        }
        if (actual != null) {
            var s = actual.snapshot();
            if (s.corriendo()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya hay una simulación en curso");
            }
            actual.stop();                       // limpia hilos de una ejecución bloqueada
        }
        actual = new ImpresionSimulation(docs, omitir, 800);
        actual.start();
        return ResponseEntity.ok(actual.snapshot());
    }

    @GetMapping("/state")
    public synchronized ResponseEntity<ImpresionSimulation.Snapshot> state() {
        if (actual == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(actual.snapshot());
    }

    @PostMapping("/reset")
    public synchronized ResponseEntity<Void> reset() {
        if (actual != null) actual.stop();
        actual = null;
        return ResponseEntity.noContent().build();
    }
}
