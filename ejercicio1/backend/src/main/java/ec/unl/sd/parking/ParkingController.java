package ec.unl.sd.parking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "http://localhost:5173")
public class ParkingController {

    private ParkingSimulation actual;

    @PostMapping("/start")
    public synchronized ResponseEntity<?> start(
            @RequestParam(defaultValue = "4") int capacity,
            @RequestParam(defaultValue = "10") int vehicles) {

        if (capacity < 1 || capacity > 20 || vehicles < 1 || vehicles > 30) {
            return ResponseEntity.badRequest()
                    .body("capacity debe estar entre 1 y 20, y vehicles entre 1 y 30");
        }
        if (actual != null && actual.snapshot().corriendo()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya hay una simulación en curso");
        }
        actual = new ParkingSimulation(capacity, vehicles, 1500, 3500);
        actual.start();
        return ResponseEntity.ok(actual.snapshot());
    }

    @GetMapping("/state")
    public synchronized ResponseEntity<ParkingSimulation.Snapshot> state() {
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
