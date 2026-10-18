# Práctica N.º 3: Sincronización de Procesos
## Integrantes

- Marco Castillo
- Viviana Córdova
- Ariel Tandazo
- Isauro Rivera

## Descripción

La presente práctica tiene como finalidad implementar mecanismos de sincronización de procesos e hilos para controlar el acceso a recursos compartidos, evitar condiciones de carrera y garantizar la ejecución coordinada de operaciones concurrentes.

## Objetivos

- Implementar barreras de sincronización y mecanismos de exclusión mutua.
- Aplicar semáforos para controlar el acceso a recursos limitados.
- Resolver problemas clásicos de concurrencia mediante hilos y procesos.
- Analizar los resultados obtenidos mediante pruebas de funcionamiento.

## Ejercicios desarrollados

**Ejercicio 1. Control de acceso a un estacionamiento — Semáforo contador**

Implementación de un semáforo contador para controlar el ingreso de vehículos a un estacionamiento con capacidad máxima de cuatro vehículos.

**Ejercicio 2. Cajero automático compartido — Exclusión mutua**

Implementación de un mecanismo mutex para proteger el saldo de una cuenta bancaria compartida, con un saldo inicial de $1000 y operaciones concurrentes de retiro y depósito.

**Ejercicio 3. Fábrica de ensamblaje — Productor-Consumidor**

Implementación de una cola limitada con capacidad para cinco piezas, utilizando semáforos y mutex para coordinar la producción y el consumo.

**Ejercicio 4. Cruce de vehículos — Control de acceso en una dirección**

Desarrollo de una simulación de un puente que permite el tránsito de vehículos en ambos sentidos, garantizando que no circulen vehículos en direcciones opuestas simultáneamente.

**Ejercicio 5. Centro de impresión — Sincronización de procesos**

Implementación de semáforos para coordinar las etapas de diseño, revisión e impresión, respetando el orden establecido.

## Tecnologías utilizadas

- Java y Spring Boot.
- React y Vite.
- Python u otros lenguajes utilizados en los ejercicios.
- Git y GitHub para el control de versiones.
