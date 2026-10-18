# Fábrica de Ensamblaje

Simulación de una fábrica por medio de programación concurrente (productor-consumidor)
con microservicios Spring Boot y frontend Flask.

## Estructura

- `simulacion-service/`: Microservicio Spring Boot. Core de concurrencia (almacén circular, productores/consumidores, Observer, Factory).
- `metricas-service/`: Microservicio Spring Boot. Recibe eventos vía REST y calcula totales/estadísticas.
- `frontend-flask/`: Frontend Flask (Python) que consume los microservicios.

## Ejecución local

```bash
# Simulacion service (puerto 8080)
cd simulacion-service && mvn spring-boot:run

# Metricas service (puerto 8081)
cd metricas-service && mvn spring-boot:run

# Frontend (puerto 5000)
cd frontend-flask && pip install -r requirements.txt && python app.py
```
