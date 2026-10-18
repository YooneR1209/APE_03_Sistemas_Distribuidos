import os
import requests
from flask import Flask, jsonify, render_template, request

app = Flask(__name__)

SIMULACION_URL = os.environ.get("SIMULACION_URL", "http://localhost:8080")
METRICAS_URL = os.environ.get("METRICAS_URL", "http://localhost:8081")
TIMEOUT = 5


def _proxy(url, metodo="GET", payload=None):
    """Patrón BFF: el frontend nunca habla directo con los microservicios."""
    try:
        if metodo == "POST":
            r = requests.post(url, json=payload, timeout=TIMEOUT)
        else:
            r = requests.get(url, timeout=TIMEOUT)
        try:
            return jsonify(r.json()), r.status_code
        except ValueError:
            return jsonify({"mensaje": r.text}), r.status_code
    except requests.exceptions.ConnectionError:
        return jsonify({"error": f"No se pudo conectar con {url}. ¿Está el microservicio en ejecución?"}), 503
    except requests.exceptions.Timeout:
        return jsonify({"error": "Tiempo de espera agotado al contactar el microservicio"}), 504


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/api/simulacion/<path:recurso>", methods=["GET", "POST"])
def proxy_simulacion(recurso):
    url = f"{SIMULACION_URL}/api/simulacion/{recurso}"
    if request.method == "POST":
        return _proxy(url, "POST", request.get_json(silent=True))
    return _proxy(url, "GET")


@app.route("/api/eventos", methods=["GET"])
def proxy_eventos():
    limite = request.args.get("limite", "150")
    return _proxy(f"{SIMULACION_URL}/api/eventos?limite={limite}", "GET")


@app.route("/api/metricas/<path:recurso>", methods=["GET", "POST"])
def proxy_metricas(recurso):
    url = f"{METRICAS_URL}/api/metricas/{recurso}"
    if request.method == "POST":
        return _proxy(url, "POST", request.get_json(silent=True))
    return _proxy(url, "GET")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
