# Cajero automático compartido — Exclusión mutua

Práctica de Sistemas Distribuidos (Ejercicio 2). Tres procesos modifican a la vez el saldo de una misma cuenta
($1000 iniciales):

| Proceso   | Operación       |
|-----------|-----------------|
| Proceso 1 | retirar $300    |
| Proceso 2 | retirar $250    |
| Proceso 3 | depositar $200  |

Saldo final esperado: **$650**. Con `ReentrantLock` siempre se obtiene; sin él, las actualizaciones pueden perderse
(condición de carrera).

## Requisitos

- Java 21
- Maven 3.9 o superior
- Node.js 18 o superior (con npm)

## Estructura

```
cajero-concurrencia/
├── backend/                       Microservicio cajero-service (Spring Boot, puerto 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── resources/application.properties
│       └── java/com/sistemasdistribuidos/cajero/
│           ├── CajeroApplication.java          Arranque de Spring Boot
│           ├── CajeroController.java           Endpoints REST (sin lógica de negocio)
│           ├── CajeroService.java              Lanza los 3 hilos y arma el resultado
│           ├── CuentaBancaria.java             Saldo, ReentrantLock, retirar() y depositar()
│           ├── Evento.java / Operacion.java / ResultadoSimulacion.java   Datos del JSON
│           └── SimulacionEnCursoException.java Error 409 si ya hay una simulación corriendo
├── frontend/                      Aplicación React + Vite (puerto 5173)
│   ├── package.json, vite.config.js, index.html
│   └── src/ main.jsx, App.jsx, ResultadoPanel.jsx, api.js, styles.css
└── README.md
```

## Cómo ejecutar

**1. Backend** (terminal 1):

```bash
cd backend
mvn spring-boot:run
```

Queda escuchando en http://localhost:8080.

**2. Frontend** (terminal 2):

```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173. Vite reenvía las llamadas `/api` al backend (ver `vite.config.js`), por eso no hace
falta configurar nada más.

## API REST

| Método | Ruta                              | Descripción                                   |
|--------|-----------------------------------|-----------------------------------------------|
| GET    | `/api/cajero/saldo`               | Saldo actual                                  |
| POST   | `/api/cajero/simular/con-mutex`   | Ejecuta los 3 procesos protegidos con mutex   |
| POST   | `/api/cajero/simular/sin-mutex`   | Ejecuta los 3 procesos sin protección (demo)  |

Cada simulación reinicia la cuenta a $1000, espera a que terminen los tres procesos y devuelve:
`modo`, `saldoInicial`, `saldoFinal`, `saldoEsperado`, `correcto`, `verificacion`, `operaciones` y `eventos`.
Si se pide una simulación mientras otra sigue corriendo, responde **409** con `{"error": "..."}`.

## Pruebas manuales

Con el backend en marcha:

```bash
curl http://localhost:8080/api/cajero/saldo
# esperado: {"saldo":1000}

curl -X POST http://localhost:8080/api/cajero/simular/con-mutex
# esperado: "saldoFinal":650, "correcto":true, 3 operaciones realizadas, 6 eventos
#           (cada proceso: "Intenta..." seguido de "Retiro/Depósito realizado")

curl -X POST http://localhost:8080/api/cajero/simular/sin-mutex
# esperado: normalmente "saldoFinal" distinto de 650 (p. ej. 700, 750 o 1200).
#           Si sale 650, la carrera no se manifestó: se repite el comando.

curl http://localhost:8080/api/cajero/saldo
# esperado: el saldo final de la última simulación
```

Error 409 (dos simulaciones a la vez): lanza dos `curl -X POST .../simular/sin-mutex` casi al mismo tiempo; la
segunda responde `409` con el mensaje de simulación en curso.

Retiro con saldo insuficiente: la simulación normal nunca lo provoca (con $1000 alcanzan los dos retiros), pero
`CuentaBancaria.retirar()` rechaza el retiro si `saldo < monto`, deja el saldo intacto y registra
"Saldo insuficiente".

En la interfaz: pulsa "Ejecutar con mutex" varias veces (siempre $650) y "Ejecutar sin mutex" varias veces
(normalmente un saldo incorrecto). Mientras corre una simulación los botones quedan deshabilitados; si apagas el
backend, la interfaz muestra un mensaje de error.

## Cómo explicarlo en la defensa

1. **El problema.** El saldo es un recurso compartido. Retirar es "leer el saldo, comprobar, restar, escribir":
   cuatro pasos que no son atómicos. Si dos hilos leen $1000 a la vez, cada uno escribe su resultado a partir de ese
   valor viejo y el último en escribir **pisa** al otro. Es una condición de carrera (actualización perdida).
2. **Sin mutex** (`retirarSinMutex`, `depositarSinMutex`). Los tres leen $1000, hacen una pausa de 50 ms entre la
   lectura y la escritura (solo para abrir la ventana de la carrera en la demostración) y escriben $700, $750 y
   $1200. El saldo final es el del último en escribir, no $650. La pausa hace la carrera muy probable, pero no
   garantizada, por eso el sistema muestra el resultado real de cada ejecución.
3. **Con mutex** (`retirar`, `depositar`). `lock.lock()` deja pasar a un solo hilo a la vez. La comprobación
   `saldo >= monto` y la resta ocurren dentro de la **misma sección crítica**, así que nadie puede cambiar el saldo
   entre medias. El `unlock()` va en un `finally` para liberar el lock aunque ocurra un error. No hay `sleep` en este
   modo: la corrección la da el lock, no el tiempo. El orden de los procesos puede variar, pero el resultado siempre
   es $650.
4. **Otros detalles.** Los tres procesos salen a la vez desde un `CountDownLatch` (solo iguala la salida, no protege
   el saldo). `CajeroService` espera a los tres hilos con `Future.get()` antes de responder. Un `AtomicBoolean`
   impide dos simulaciones simultáneas, porque cada una reinicia la cuenta.

> El modo sin mutex es exclusivamente educativo y no debe usarse en operaciones bancarias reales.
