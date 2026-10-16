# Ejercicio 5 — Centro de impresión (semáforos)

Spring Boot 3 (Java 17) + React (Vite). Orden obligatorio por documento: DISEÑO → REVISIÓN → IMPRESIÓN.

## Ejecutar
```bash
# Terminal 1 - backend (puerto 8081)
cd backend && mvn spring-boot:run

# Terminal 2 - frontend (puerto 5174)
cd frontend && npm install && npm run dev
```
Abrir http://localhost:5174

## Pruebas
```bash
cd backend && mvn test
```

## Estructura
- `ImpresionSimulation`: tres `Semaphore` (`turnoDiseno=1`, `turnoRevision=0`, `turnoImpresion=0`) y tres hilos.
- `ImpresionController`: `POST /api/impresion/start?docs=3[&omitir=DISENO|REVISION|IMPRESION]`, `GET /state`, `POST /reset`.
- El parámetro `omitir` elimina un `signal()` para responder el punto 9 de la guía (la simulación se bloquea).
