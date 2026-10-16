# Ejercicio 1 — Estacionamiento con semáforo contador

Spring Boot 3 (Java 17) + React (Vite).

## Ejecutar
```bash
# Terminal 1 - backend (puerto 8080)
cd backend && mvn spring-boot:run

# Terminal 2 - frontend (puerto 5173)
cd frontend && npm install && npm run dev
```
Abrir http://localhost:5173

## Pruebas (criterio de verificación: nunca más de 4 dentro)
```bash
cd backend && mvn test
```

## Estructura
- `ParkingSimulation`: semáforo `Semaphore(capacidad, true)`; un hilo por vehículo (`acquire` = wait, `release` = signal).
- `ParkingController`: `POST /api/parking/start?capacity=4&vehicles=10`, `GET /api/parking/state`, `POST /api/parking/reset`.
- `frontend/src/App.jsx`: consulta el estado cada 300 ms y dibuja estacionamiento, cola y registro.
