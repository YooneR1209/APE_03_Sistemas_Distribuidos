# 🌉 Simulación Concurrente de Puente — Exclusión Mutua y Anti-Inanición

Este proyecto es una aplicación **Full-Stack** que simula el paso de vehículos a través de un puente de un solo carril (Recurso Compartido). Implementa mecanismos estrictos de concurrencia y exclusión mutua mediante **Spring Boot** en el backend y visualización dinámica en tiempo real utilizando **React**.

---

## 📐 Arquitectura del Sistema

El sistema utiliza una **arquitectura cliente-servidor desacoplada** con opción de ejecución local o despliegue en **contenedores independientes**:

* **Backend (API REST):** Desarrollado en **Spring Boot (Java 17)**. Controla el estado del puente, los contadores de tráfico y gestiona la concurrencia e hilos mediante `ReentrantLock` y bucles de sincronización para prevenir la inanición (*starvation*).
* **Frontend (SPA):** Desarrollado en **React + Vite**. Mantiene un sondeo constante (*polling*) para renderizar las animaciones del flujo vehicular de Norte a Sur y viceversa de forma fluida.
* **Infraestructura:** Orquestación multi-contenedor mediante **Docker y Docker Compose**.

---

## ⚙️ Reglas de Concurrencia y Negocio

1. **Exclusión Mutua:** Un solo sentido (Norte o Sur) puede cruzar el puente a la vez.
2. **Capacidad del Puente:** Los vehículos avanzan secuencialmente guardando tiempos de paso para evitar colisiones.
3. **Control Anti-Inanición:** Se establece un límite máximo de **6 vehículos consecutivos por sentido** (`MAX_CONSECUTIVOS = 6`). Al alcanzar este límite, se fuerza el cambio de turno para dar paso al flujo opuesto.

---

## 💻 Comandos de Ejecución Local (Sin Docker)

Si prefieres ejecutar los proyectos localmente desde la terminal sin utilizar contenedores:

### 1. Backend (Spring Boot)
Abre una terminal en la carpeta `backend/`:

  ```bash
  cd backend
  ./mvnw spring-boot:run

### 2. Frontend (React)
Abre una terminal en la carpeta `frontend/`:

    ```bash
    cd frontend
    npm install
    npm run dev