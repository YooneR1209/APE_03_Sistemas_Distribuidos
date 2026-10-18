package com.fabrica.simulacion.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fabrica.simulacion.model.EventoSimulacion;
import com.fabrica.simulacion.model.TipoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Observador 2: propaga eventos al microservicio de métricas (fire-and-forget, tolera caídas). */
@Component
public class PublicadorMetricas implements ObservadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorMetricas.class);
    private static final Set<TipoEvento> RELEVANTES =
            Set.of(TipoEvento.PRODUCIDO, TipoEvento.CONSUMIDO, TipoEvento.ANOMALIA);

    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${metricas.url:http://localhost:8081}")
    private String urlMetricas;

    @Override
    public void onEvento(EventoSimulacion evento) {
        if (!RELEVANTES.contains(evento.getTipo())) return;
        enviar("/api/metricas/evento", Map.of(
                "tipo", evento.getTipo().name(),
                "actor", evento.getActor() == null ? "SISTEMA" : evento.getActor(),
                "mensaje", evento.getMensaje() == null ? "" : evento.getMensaje()));
    }

    public void reiniciarMetricas() {
        enviar("/api/metricas/reiniciar", Map.of());
    }

    private void enviar(String ruta, Map<String, Object> cuerpo) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(urlMetricas + ruta))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                    .build();
            cliente.sendAsync(peticion, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((resp, error) -> {
                        if (error != null) log.debug("metricas-service no disponible: {}", error.getMessage());
                    });
        } catch (Exception e) {
            log.debug("No se pudo publicar la métrica: {}", e.getMessage());
        }
    }
}
